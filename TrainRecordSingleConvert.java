package xwp.jp.bns;

import agile.integration.db.mongo.MongoHelperEx;
import agile.integration.utils.JsonHelper;
import com.mongodb.client.MongoDatabase;
import org.apache.http.client.fluent.Executor;
import org.apache.log4j.Logger;
import xwp.comm.CommandMessage;
import xwp.jp.bns.comm.DbHelper;
import xwp.jp.entity.TrainRecordSingleEntity;
import xwp.jp.entity.TrainRecordSingleReturnEntity;
import xwp.jp.src.train.TrainRecordSingleActionModel;
import xwp.jp.utils.ESUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by DaiHao on 2018-07-11.
 */
public class TrainRecordSingleConvert {

    //private static long START_TIME = 1493568000000L; // 开始时间
    private static long START_TIME = 1531756800000L; // 开始时间
    private static long END_TIME = 0;   //结束时间
    private static long END_FIRST_TIME = 1531843200000L;   //一次执行一天
    private static long DAY_HOURS = 86400000L; //一天的时间戳
    private static long HOURS = 3600000; //一小时时间戳
    private static final int COUNT = 300; //每次插入ES条数
    private static final long ENDTIME_FINAL = 1532055600000L;
    private static Logger logger = Logger.getLogger(TrainRecordSingleConvert.class);
    private static final String TYPE = "jp_train_record_single";

    public static void executeDataSync(String[] tagMessage) {

        MongoDatabase db = null;
        try {
            db = DbHelper.GetMongoDb();
            if (null == db) {
                logger.error("获取mongo的实例失败");
                return;
            }

            while (true) {
                Vector<Thread> threads = new Vector<>();
                while (true) {
                    //结束时间
                    END_TIME = START_TIME + HOURS;
                    String key = TrainRecordSingleActionModel.Prefix_Mongo_Train.value();
                    //小于结束的时间，大于等于开始的时间
                    String jquery = String.format("{ctm:{$lt:%s,$gte:%s}}", String.valueOf(END_TIME), String.valueOf(START_TIME));
                    String jfileds = "{_id:1,sid:1,snm:1,path:1,cid:1,cnm:1,vid:1,pno:1,spt:1,crst:1," +
                            "tnt:1,rcdsta:1,rdtm:1,stm:1,etm:1,ctm:1,vmile:1,vmmile:1,shltm:1}";
                    String jsort = "{ctm:1}";
                    final List<TrainRecordSingleReturnEntity> select0 = MongoHelperEx.Select(db, TrainRecordSingleReturnEntity.class, key, jquery, jfileds, jsort, 0, 10000000, tagMessage);
                    if (null == select0 || select0.size() == 0) {
                        logger.info("query data to mongo null:" + jquery);
                        if (START_TIME >= END_FIRST_TIME)
                            break;
                        START_TIME += HOURS;
                        continue;
                    }
                    //新建子线程将数据插入ES数据库
                    Thread th = new Thread(() -> {
                        List<TrainRecordSingleReturnEntity> select = select0;
                        List<String> ids = new ArrayList<String>();
                        Object[] results = new Object[]{null};
                        StringBuilder sb = new StringBuilder();
                        int count = 0;
                        for (TrainRecordSingleReturnEntity entity : select) {
                            ids.add(entity._id);
                            count++;
                            sb.append(String.format("{\"index\":{\"_id\":\"%s\"}}\n", entity._id));
                            sb.append(packagIndexJsonByEntity(entity));
                            if (count >= COUNT) {
                                ESUtils.putDocuments(TYPE, sb.toString(), results, ids);
                                count = 0;
                                sb = new StringBuilder();
                                //清空list
                                ids.clear();
                            }
                        }
                        if (ids.size() > 0) {
                            ESUtils.putDocuments(TYPE, sb.toString(), results, ids);
                        }
                        logger.info("thread end!satrt: " + select.get(0).ctm + "  end: " + select.get(select.size() - 1).ctm);
                    });
                    threads.add(th);
                    th.start();

                    if (START_TIME >= END_FIRST_TIME)
                        break;
                    START_TIME += HOURS;
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //等待所有子线程执行完毕
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                logger.info("all thread end : " + END_TIME);
                if (END_FIRST_TIME >= ENDTIME_FINAL) {
                    break;
                }
                END_FIRST_TIME += DAY_HOURS;
            }
        } catch (Exception ex) {
            logger.error("---------->导入ES的电子教学日志出现异常 :" + ex.getMessage());
            return;
        }
    }

    /**
     * 批量插入电子日志
     *
     * @return
     */
    public static long batchInsertTrainRecordSingle(List<TrainRecordSingleReturnEntity> select, List<Long> ids) {
        Object[] object = new Object[]{null};
        String s = TrainRecordSingleConvert.packageBatchInsertJsonByEntity(select);
//        long re = ESUtils.putDocuments("jp_train_record_single", s, object, ids);
        return CommandMessage.success;
    }


   /* public static long insertTrainRecordSingleById(MongoDatabase db, String id, String[] tagMessage) {
        String key = TrainRecordSingleActionModel.Prefix_Mongo_Train.value();
        String jquery = String.format("{_id:\"%s\"}", id);
//        String jquery = String.format("{ctm:{$lte:%s,$gte:%s}}", "1495780836257", "1495710911299");
        String jfileds = "{_id:1,sid:1,snm:1,path:1,cid:1,cnm:1,vid:1,pno:1,spt:1,crst:1," +
                "tnt:1,rcdsta:1,rdtm:1,stm:1,etm:1,ctm:1,vmile:1,vmmile:1,shltm:1}";
        String jsort = "{ctm:-1}";
//        List<TrainRecordSingleReturnEntity> select = MongoHelperEx.Select(db, TrainRecordSingleReturnEntity.class, key, jquery, jfileds, jsort, 0, 50, tagMessage);
        TrainRecordSingleReturnEntity entity = MongoHelperEx.SelectSingle(db, TrainRecordSingleReturnEntity.class, key, jquery, jfileds, tagMessage);
        if (null == entity){
            return CommandMessage.success;
        }
        Object[] object = new Object[]{null};
//        String s = TrainRecordSingleConvert.packageBatchInsertJsonByEntity(select);
//        long re = ESUtils.putDocuments("jp_train_record_single", s, object, tagMessage);
        String s = TrainRecordSingleConvert.packageJsonByEntity(entity);
        long re = ESUtils.putDocument("jp_train_record_single", entity._id, s, object, tagMessage);

        ESResponse esResponse = JsonHelper.ToObject((String) object[0], ESResponse.class);
        if (CommandMessage.exception == re) {
            //发生异常的处理
            logger.error("---------->导入ES的电子教学日志出现异常");
            logger.error("------->出现异常的电子日志记录数据id为： （" + entity._id + "）");
        }

        if (esResponse.errors) {
            logger.error("---------->---------------------------->>>>object[0]");
            //插入失败的处理
            logger.error("---------->导入ES的电子教学日志失败");
            logger.error("------->失败的电子日志记录数据id为： （" + entity._id + "）");
        }
        return CommandMessage.success;

    }
*/

    /*public static String packageJsonByEntity(TrainRecordSingleReturnEntity entity) {
        String create = "{\"ph\":\"" + entity.path + "\",\"id\":\"" + entity.sid + "\",\"nm\":\"" + entity.snm + "\"," +
                    "\"coid\":\"" + entity.cid + "\",\"conm\":\"" + entity.cnm + "\",\"vid\":\"" + entity.vid + "\"," +
                    "\"vno\":\"" + entity.pno + "\",\"prgs\":\"" + entity.spt + "\",\"crst\":\"" + entity.crst + "\"," +
                    "\"ttp\":\"" + entity.tnt + "\",\"regs\":\"" + entity.rcdsta + "\",\"regt\":\"" + entity.rdtm + "\"," +
                    "\"stm\":\"" + entity.stm + "\",\"etm\":\"" + entity.etm + "\",\"ctm\":\"" + entity.ctm + "\"," +
                    "\"efh\":\"" + entity.shltm + "\",\"efm\":\"" + entity.vmile + "\",\"mspd\":\"" + entity.vmmile + "\"," +
                    "\"rid\":\"" + entity._id + "\"}";
        return create;
    }*/

    /**
     * 封装成批量插入的json
     *
     * @param list
     * @return
     */
    public static String packageBatchInsertJsonByEntity(List<TrainRecordSingleReturnEntity> list) {
        StringBuffer sb = new StringBuffer();
        for (TrainRecordSingleReturnEntity entity : list) {
            String index = String.format("{\"ph\":%s," +
                            "\"id\":%s," +
                            "\"nm\":\"%s\"," +
                            "\"coid\": %s," +
                            "\"conm\":\"%s\"," +
                            "\"vid\": %s," +
                            "\"vno\": \"%s\"," +
                            "\"prgs\": %s," +
                            "\"crst\": %s," +
                            "\"ttp\": \"%s\"," +
                            "\"regs\": %s," +
                            "\"regt\": %s," +
                            "\"stm\": %s," +
                            "\"etm\": %s," +
                            "\"ctm\": %s," +
                            "\"efh\": %s," +
                            "\"efm\": %s," +
                            "\"mspd\": %s," +
                            "\"rid\": \"%s\"}\n", entity.path, entity.sid, entity.snm, entity.cid, entity.cnm, entity.vid,
                    entity.pno, entity.spt, entity.crst, entity.tnt, entity.rcdsta, entity.rdtm,
                    entity.stm, entity.etm, entity.ctm, entity.shltm, entity.vmile, entity.vmmile, entity._id);

            /*String create = "{\"ph\":" + entity.path + ",\"id\":" + entity.sid + ",\"nm\":\"" + entity.snm + "\"," +
                    "\"coid\":" + entity.cid + ",\"conm\":\"" + entity.cnm + "\",\"vid\":" + entity.vid + "," +
                    "\"vno\":\"" + entity.pno + "\",\"prgs\":" + entity.spt + ",\"crst\":" + entity.crst + "," +
                    "\"ttp\":\"" + entity.tnt + "\",\"regs\":" + entity.rcdsta + ",\"regt\":" + entity.rdtm + "," +
                    "\"stm\":" + entity.stm + ",\"etm\":" + entity.etm + ",\"ctm\":" + entity.ctm + "," +
                    "\"efh\":" + entity.shltm + ",\"efm\":" + entity.vmile + ",\"mspd\":" + entity.vmmile + "," +
                    "\"rid\":\"" + entity._id + "\"}\n";*/
            String id = String.format("{\"index\":{\"_id\":\"%s\"}}\n", entity._id);
            String json = id + index;
            sb.append(json);
        }
        return sb.toString();
    }

    /**
     * 封装index的json
     *
     * @param entity
     * @return
     */
    public static String packagIndexJsonByEntity(TrainRecordSingleReturnEntity entity) {
        TrainRecordSingleEntity singleEntity = new TrainRecordSingleEntity();
        singleEntity.rid = entity._id;
        singleEntity.ph = entity.path;
        singleEntity.id = entity.sid;
        singleEntity.nm = entity.snm;
        singleEntity.coid = entity.cid;
        singleEntity.conm = entity.cnm;
        singleEntity.vid = entity.vid;
        singleEntity.vno = entity.pno;
        singleEntity.prgs = entity.spt;
        singleEntity.crst = entity.crst;
        singleEntity.ttp = entity.tnt;
        singleEntity.regs = entity.rcdsta;
        singleEntity.regt = entity.rdtm;
        singleEntity.stm = entity.stm;
        singleEntity.etm = entity.etm;
        singleEntity.ctm = entity.ctm;
        singleEntity.efh = entity.shltm;
        singleEntity.efm = entity.vmile;
        singleEntity.mspd = entity.vmmile;

        return JsonHelper.ToJson(singleEntity) + "\n";
    }

    /**
     * 封装成批量修改的json
     *
     * @param list
     * @return public static String packageBatchUpdateJsonByEntity(List<TrainRecordSingleReturnEntity> list) {
    StringBuffer sb = new StringBuffer();
    for (TrainRecordSingleReturnEntity entity : list) {
    String update = "{\"ph\":\"" + entity.path + "\",\"id\":\"" + entity.sid + "\",\"nm\":\"" + entity.snm + "\"," +
    "\"coid\":\"" + entity.cid + "\",\"conm\":\"" + entity.cnm + "\",\"vid\":\"" + entity.vid + "\"," +
    "\"vno\":\"" + entity.pno + "\",\"prgs\":\"" + entity.spt + "\",\"crst\":\"" + entity.crst + "\"," +
    "\"ttp\":\"" + entity.tnt + "\",\"regs\":\"" + entity.rcdsta + "\",\"regt\":\"" + entity.rdtm + "\"," +
    "\"stm\":\"" + entity.stm + "\",\"etm\":\"" + entity.etm + "\",\"ctm\":\"" + entity.ctm + "\"," +
    "\"efh\":\"" + entity.shltm + "\",\"efm\":\"" + entity.vmile + "\",\"mspd\":\"" + entity.vmmile + "\"}";
    String json = "{ \"update\":{\"_id\":\"" + entity._id + "\"}}" + "\n" + update + "\n";
    sb.append(json);
    }
    return sb.toString();
    }
     */
}
