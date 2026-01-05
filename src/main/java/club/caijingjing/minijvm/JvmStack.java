package club.caijingjing.minijvm;

import java.util.ArrayDeque;
import java.util.Deque;

public class JvmStack {
	Deque<StackFrame> stack;
	public JvmStack() {
		this.stack = new ArrayDeque<>();
	}
	public void push(StackFrame stackFrame) {
		stack.push(stackFrame);
	}
	public StackFrame pop() {
		return stack.pop();
	}
	public StackFrame peek() {
		return stack.peek();
	}
	public boolean isEmpty() {
		return stack.isEmpty();
	}
}
