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
	final Object[] localVariable;
	// 操作数栈
	final Deque<Object> operandStack;
	// 常量池
	final ConstantPool constantPool;
	// 方法的字节码指令列表
	final List<Instruction> instructions;
	// 当前指令的索引
	private int pcr = 0;
	public StackFrame(MethodInfo methodInfo, ConstantPool constantPool, Object... args) {
		this.methodInfo = methodInfo;
		this.constantPool = constantPool;
		this.localVariable = new Object[methodInfo.getMaxLocals()];
		this.operandStack = new ArrayDeque<>();
		this.instructions = methodInfo.getCodes();
		System.arraycopy(args,0,localVariable,0,args.length);
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

	public void jumpTo(int index) {
		for (int i = 0; i < instructions.size(); i++) {
			Instruction instruction = instructions.get(i);
			if(instruction.getPc() == index) {
				this.pcr = i;
				return;
			}
		}
	}
}
