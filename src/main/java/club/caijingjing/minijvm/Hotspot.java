package club.caijingjing.minijvm;

import tech.medivh.classpy.classfile.ClassFile;

import java.io.File;
import java.util.Arrays;

public class Hotspot {
	// 类加载器
	private BootstrapClassLoader classLoader;
	private String mainClass;
	public Hotspot(String mainClassName, String mainClassPath) {
		/* 两个参数：一个是类名，一个是类路径
		* File.pathSeparator：Mac 和 Linux 下是冒号 :，Windows 下是分号 ; 表示路径分隔符
		* */
		this.mainClass = mainClassName;
		this.classLoader = new BootstrapClassLoader(Arrays.asList(mainClassPath.split(File.pathSeparator)));
	}
	public void start() throws Exception {
		// 将主类编译成字节码文件，也就是demo.class 文件加载到内存中
		ClassFile classFile =  classLoader.loadClass(mainClass);
		// 创建 main 方法 的栈帧
		StackFrame mainFrame = new StackFrame( classFile.getMainMethod(), classFile.getConstantPool());
		// 创建主线程，并将 main 方法的栈帧压入主线程的栈中
		Thread mainThread = new Thread("main", mainFrame, classLoader);
		mainThread.start();
	}
}
