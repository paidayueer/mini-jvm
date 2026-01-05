package club.caijingjing.minijvm;

import tech.medivh.classpy.classfile.MethodInfo;
import tech.medivh.classpy.classfile.bytecode.Instruction;
import tech.medivh.classpy.classfile.constant.ConstantPool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class StackFrame {
	// 方法信息
	final MethodInfo methodInfo;
	// 局部变量表
	final List<Object> localVariable;
	// 操作数栈
	final Deque<Object> operandStack;
	// 常量池
	final ConstantPool constantPool;
	// 方法的字节码指令列表
	final List<Instruction> instructions;
	// 当前指令的索引
	private int pcr = 0;
	public StackFrame(MethodInfo methodInfo, ConstantPool constantPool) {
		this.methodInfo = methodInfo;
		this.constantPool = constantPool;
		this.localVariable = new ArrayList<>();
		this.operandStack = new ArrayDeque<>();
		this.instructions = methodInfo.getCodes();
	}
	public Instruction getNextInstruction() {
		return instructions.get(pcr++);
	}


	public ConstantPool constantPool() {
		return constantPool;
	}

	public void pushObjectToOperandStack(Object o) {
		operandStack.push(o);
	}
}
