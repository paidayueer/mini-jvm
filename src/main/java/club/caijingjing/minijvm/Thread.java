package club.caijingjing.minijvm;

import tech.medivh.classpy.classfile.ClassFile;
import tech.medivh.classpy.classfile.MethodInfo;
import tech.medivh.classpy.classfile.bytecode.*;
import tech.medivh.classpy.classfile.constant.ConstantInfo;
import tech.medivh.classpy.classfile.constant.ConstantMethodrefInfo;
import tech.medivh.classpy.classfile.constant.ConstantPool;
import tech.medivh.classpy.classfile.constant.ConstantStringInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class Thread {
	private String threadName;
	private JvmStack jvmStack;
	private BootstrapClassLoader classLoader;

	public Thread(String threadName, StackFrame stackFrame, BootstrapClassLoader classLoader) {
		this.threadName = threadName;
		this.classLoader = classLoader;
		this.jvmStack = new JvmStack();
		this.jvmStack.push(stackFrame);
	}
	public void start() throws Exception {
		while(!jvmStack.isEmpty()) {
			ConstantPool constantPool = jvmStack.peek().constantPool();
			Instruction currentInstruction = jvmStack.peek().getNextInstruction();
			switch (currentInstruction.getOpcode()) {
				case getstatic -> {
					GetStatic getStatic = (GetStatic) currentInstruction;
					String className = getStatic.getClassName(constantPool);
					String fieldName = getStatic.getFieldName(constantPool);
					Object staticField;
					// 这里直接用反射获取静态字段的值
					if(className.contains("java")) {
						Class<?> clazz = Class.forName(className);
						Field declaredField = clazz.getDeclaredField(fieldName);
						// get(null) 静态字段不需要对象实例
						staticField = declaredField.get(null);
						jvmStack.peek().pushObjectToOperandStack(staticField);
					}
				}
				/*	字符串走ldc，但是库里的 loadDesc 是 protect 类型，先注释掉
				case ldc -> {
					Ldc ldc = (Ldc) currentInstruction;
					ConstantStringInfo constantInfo = (ConstantStringInfo) ldc.getConstantInfo(constantPool);
					jvmStack.peek().pushObjectToOperandStack(constantInfo.loadDesc(constantPool));
				}*/
				case iconst_0 -> jvmStack.peek().pushObjectToOperandStack(0);
				case iconst_1 -> jvmStack.peek().pushObjectToOperandStack(1);
				case iconst_2 -> jvmStack.peek().pushObjectToOperandStack(2);
				case iconst_3 -> jvmStack.peek().pushObjectToOperandStack(3);
				case iconst_4 -> jvmStack.peek().pushObjectToOperandStack(4);
				case iconst_5 -> jvmStack.peek().pushObjectToOperandStack(5);
				case iload_0 -> jvmStack.peek().pushObjectToOperandStack(jvmStack.peek().localVariable[0]);
				case iload_1 -> jvmStack.peek().pushObjectToOperandStack(jvmStack.peek().localVariable[1]);
				// todo：调用虚方法，虚方法是属于对象的方法，需要通过对象来调用，这里利用反射，注意参数的传递顺序，是从后往前取的，因为操作数栈是后进先出，数量上是参数数量 + 1（对象本身），所以参数数组长度是 params.size() + 1
				case invokevirtual -> {
					InvokeVirtual invokeVirtual = (InvokeVirtual) currentInstruction;
					ConstantMethodrefInfo methodInfo = invokeVirtual.getMethodInfo(constantPool);
					String className = methodInfo.className(constantPool);
					String methodName = methodInfo.methodName(constantPool);
					List<String> params = methodInfo.paramClassName(constantPool);
					if(className.contains("java")) {
						Class<?> clazz = Class.forName(className);
						// Class[]::new 表示创建一个 Class 数组 ，toArray(Class[]::new) 会将流中的元素收集到一个新的 Class 数组中
						// getDeclaredMethod 的第二个参数 Class<?>...和 Class[] 是等价的
						Method declaredMethod = clazz.getDeclaredMethod(methodName,params.stream().map(this::nameToClass).toArray(Class[]::new));
						// 准备参数，注意顺序
						Object[] args = new Object[params.size()];
						for(int i = params.size() - 1;i>=0;i--) {
							args[i] = jvmStack.peek().operandStack.pop();
						}
						// 第一个参数是对象本身 存在操作数栈顶，为啥？？ 因为调用方法的时候，是先把对象引用压入操作数栈，然后才是参数，什么时候压进去的？？？
						Object result = declaredMethod.invoke(jvmStack.peek().operandStack.pop(),args);
						if(!methodInfo.isVoid(constantPool)) {
							jvmStack.peek().pushObjectToOperandStack(result);
						}
						break;
					}
					ClassFile classFile = classLoader.loadClass(className);
					MethodInfo finalMethodInfo = classFile.getMethods(methodName).get(0);
					// args 数组长度是 参数数量 + 1（对象本身）
					Object[] args = new Object[params.size() + 1];
					for(int i = args.length - 1;i>=0;i--) {
						args[i] = jvmStack.peek().operandStack.pop();
					}
					StackFrame stackFrame = new StackFrame(finalMethodInfo, classFile.getConstantPool(), args);
					jvmStack.push(stackFrame);
				}
				case invokestatic -> {
					InvokeStatic invokeStatic = (InvokeStatic) currentInstruction;
					ConstantMethodrefInfo methodInfo = invokeStatic.getMethodInfo(constantPool);
					String className = methodInfo.className(constantPool);
					String methodName = methodInfo.methodName(constantPool);
					List<String> params = methodInfo.paramClassName(constantPool);
					if(className.contains("java")) {
						Class<?> clazz = Class.forName(className);
						Method declaredMethod = clazz.getDeclaredMethod(methodName,params.stream().map(this::nameToClass).toArray(Class[]::new));
						Object[] args = new Object[params.size()];
						for(int i = params.size() - 1;i>=0;i--) {
							args[i] = jvmStack.peek().operandStack.pop();
						}
						// 静态方法没有对象实例，传 null
						Object result = declaredMethod.invoke(null,args);
						if(!methodInfo.isVoid(constantPool)) {
							jvmStack.peek().pushObjectToOperandStack(result);
						}
					}
					ClassFile classFile = classLoader.loadClass(className);
					MethodInfo finalMethodInfo = classFile.getMethods(methodName).get(0);
					// args 数组长度是 参数数量 ，因为 static 方法没有对象实例，无需传递对象本身
					Object[] args = new Object[params.size()];
					for(int i = args.length - 1;i>=0;i--) {
						args[i] = jvmStack.peek().operandStack.pop();
					}
					StackFrame stackFrame = new StackFrame(finalMethodInfo, classFile.getConstantPool(), args);
					jvmStack.push(stackFrame);
				}
				case _return -> {
					jvmStack.pop();
				}
				case ireturn -> {
					int result = (int) jvmStack.peek().operandStack.pop();
					jvmStack.pop();
					jvmStack.peek().pushObjectToOperandStack(result);
				}
				case if_icmple -> {
					int value2 = (int)jvmStack.peek().operandStack.pop();
					int value1 = (int)jvmStack.peek().operandStack.pop();
					if(value1 <= value2) {
						Branch branch = (Branch) currentInstruction;
						int jumpPc = branch.getJumpTo();
						// 跳转到指定指令
						jvmStack.peek().jumpTo(jumpPc);
					}
				}
				default -> throw new UnsupportedOperationException("不支持的指令: " + currentInstruction);
			}
		}
	}
	public Class<?> nameToClass(String name) {
		if(name == "int") {
			return int.class;
		}
		try {
			return Class.forName(name);
		}
		catch(Exception e) {
			return null;
		}
	}
}
