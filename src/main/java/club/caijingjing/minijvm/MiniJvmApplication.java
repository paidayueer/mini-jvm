package club.caijingjing.minijvm;

public class MiniJvmApplication {

	public static void main(String[] args) {
		Hotspot hotspot = new Hotspot("club.caijingjing.minijvm.mainClass.MainDemo",
				"D:\\FZU\\Projects\\shengsheng\\mini-jvm\\target\\classes");
		hotspot.start();
	}

}
