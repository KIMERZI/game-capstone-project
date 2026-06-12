import loot.GameFrameSettings;

public class Index {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
    	GameFrameSettings settings = new GameFrameSettings();
    	
    	settings.window_title = "Ghost Runner";
    	settings.canvas_height = 1000;
    	settings.canvas_width = 1600;
    	settings.numberOfButtons = 8;

    	MainFrame window = new MainFrame(settings);
    	window.setVisible(true);
    }
}
