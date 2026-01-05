package club.caijingjing.minijvm;

import tech.medivh.classpy.classfile.ClassFile;
import tech.medivh.classpy.classfile.ClassFileParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static java.nio.file.Files.readAllBytes;

/**
 * 引导类加载器
 * */
public class BootstrapClassLoader {
	private final List<String> classPaths;

	public BootstrapClassLoader(List<String> classPaths) {
		this.classPaths = classPaths;
	}

	/*
	 * 加载类文件
	 * */
	public ClassFile loadClass(String fqcn) throws ClassNotFoundException {
		return classPaths.stream()
				.map(classPath -> tryLoad(classPath, fqcn))
				.filter(Objects::nonNull)
				.findAny()
				.orElseThrow(()-> new ClassNotFoundException(fqcn + "找不到"));
		/*Stream 流式接口
		 * 用于处理集合数据，是接口，不是一种数据类型，将集合数据转换为流进行处理，不然就需要使用 for
		 * map、filter ：返回值是 Stream 类型，所以可以继续链式调用
		 * findAny：返回值是 Optional 类型，表示可能有值，也可能没有值
		 * Optional ：一个容器对象，可能包含也可能不包含非空值，属于 final 类，不能被继承
		 * orElseThrow：Optional 类的方法， 如果 Optional 有值，返回值，否则抛出异常
		 * */
		// 解释一下这个 OrElseThrow 方法的实现逻辑: (先看源码实现)
		//  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		//         if (value != null) {
		//             return value;
		//         } else {
		//             throw exceptionSupplier.get();
		//         }
		//     }
		// 返回值为 T, 表示 Optional 容器中的值类型
		// 1.（！！！） <X extends Throwable> 和 返回值 T 无关，用来定义一个泛型 X，表示异常类型，X 必须是 Throwable 的子类
		// 2. 入参 Supplier<? extends X> exceptionSupplier 是一个函数式接口，用来提供异常对象
		// 3. 逻辑第一行的 value 是 Optional 类的一个私有成员变量，表示容器中的值，只有当没有值的时候，才会用到入参 exceptionSupplier

	}

	/*
	 * 插播知识点：maven 的 compiler 插件 会调用 javac（java-compiler） 来将 java代码预编译成 class 字节码文件 放入 target 文件夹中，class文件是二进制文件，idea打开显示java格式，是因为进行了一次反编译，用其他记事本打开就会乱码。
	 * tryLoad 方法：尝试从指定路径加载类文件，按 JVM 规范解析 class 文件 生成 ClassFile 对象，里面包括类的各种信息，比如常量池、方法、字段等。
	 * 插播知识点： JVM 规范：https://docs.oracle.com/javase/specs/jvms/se7/html/
	 * 插播知识点：真正的 jvm 为了优化性能，不会生产 真正的一个 classfile 对象，解析是一个迅速的中间过程，但是过程是完全正确的，按照 jvm规范进行解析。
	 * */
	private ClassFile tryLoad(String classPath, String mainClass) {
		File classFilePath = new File(classPath, mainClass.replace(".", File.separator) + ".class");
		if(!classFilePath.exists()) {
			return null;
		}
		try {
			byte[] bytes = Files.readAllBytes(classFilePath.toPath());
			return new ClassFileParser().parse(bytes);
		}
		catch (Exception e) {
			// 这里就不抛异常了，因为这个函数被调用在 loadClass 方法中的箭头函数里，抛异常会中断流式处理
			return null;
		}
	}
}
