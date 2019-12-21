### CopyOnWrite容器

#### 什么是CopyOnWrite容器

​		其基本思路是，从一开始大家都在共享同一个内容，当某个人想要修改这个内容的时候，才会真正把内容Copy出去形成一个新的内容然后再改，这是一种延时懒惰策略

​		`CopyOnWrite` 容器即写时复制的容器。通俗的理解是当我们往一个容器添加元素的时候，不直接往当前容器添加，而是<font color=red>**先将当前容器进行Copy，复制出一个新的容器，然后新的容器里添加元素，添加完元素之后，再将原容器的引用指向新的容器**</font>

​		这样做的好处是我们可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素。所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器

> ​		从JDK1.5开始Java并发包里提供了两个使用 `CopyOnWrite` 机制实现的并发容器,它们是 `CopyOnWriteArrayList` 和 `CopyOnWriteArraySet`
>
> ​		`CopyOnWrite`容器非常有用，可以在非常多的并发场景中使用到

#### CopyOnWrite的使用

​		Cow可以解决list在并发场景下读和写同时操作下不会出现异常。但是Cow会有内存开销过大的问题，因为 `CopyOnWrite` 的写时复制机制，所以在进行写操作的时候，内存里会同时驻扎两个对象的内存，旧的对象和新写入的对象；如果数据量过大，那么对性能和内存开销会变得很大，这种场景下不建议使用。因此它更适合读多写少的场景（<font color=green>**用作缓存**</font>）

````java
public class Test {
	public static void main(String[] args) throws Exception {
		List<String> a = new ArrayList<String>();
		a.add("a");
		a.add("b");
		a.add("c");
		final CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<String>(a);
		Thread t = new Thread(new Runnable() {
			int count = -1;
			@Override
			public void run() {
				while (true) {
					list.add(count++ + "");
				}
			}
		});
		t.setDaemon(true);
		t.start();
		Thread.currentThread().sleep(3);
        //并发读写不会抛出异常
		for (String s : list) {
			System.out.println(list.hashCode());
			System.out.println(s);
		}
	}
}
````











