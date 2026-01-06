package club.caijingjing.minijvm;

public class MiniJvmApplication {

	public static void main(String[] args) throws Exception {
		Hotspot hotspot = new Hotspot("mainClass.MainDemo",
				"D:\\FZU\\Projects\\mini-jvm\\target\\classes");
		hotspot.start();
	}

}
