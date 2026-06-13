import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import loot.GameFrame;
import loot.GameFrameSettings;
import loot.graphics.DrawableObject;
import loot.graphics.Layer;
import loot.graphics.TextBox;

public class MainFrame extends GameFrame {

	/**
	 * 0 : 준비
	 * 1 : 진행
	 * 2 : 종료
	 * 3 : 정지
	 */
	int state;
	
	/**
	 * 각 state에 따른 layer
	 */
	Layer layer_start;
	Layer layer_end;
	Layer layer_pause;
	Layer layer_minimap;
	
	User us;
	ArrayList<Target> targets;
	
	TextBox title_tb;
	TextBox start_tb;
	TextBox end_tb;
	
	int init_target_number; //초기 타겟 수
	double shoot_rate; // 타겟 히트박스 크기
	double fail_rate; // 실패 시 히트박스
	
	double shake_power; //사격 시 흔들림 강도
	long shake_duration; //사격 시 흔들림 지속시간
	long shake_end_time;
	
	double[] length_to_wall;
	
	double minimap_scale;
	
	final double init_pos_x = 5, init_pos_y = 5; //초기 위치
	final double init_dir_x = 0.0, init_dir_y = -1.0; //초기 바라보는 방향
	
	double pos_x, pos_y; //현재 위치
	double dir_x, dir_y; //바라보는 방향 벡터
	double plane_x, plane_y; //시야각에 대한 법선 벡터
	
	long init_time; //시작시간
	long game_time; //현재 흐른 시간
	long check_time; //타겟 스폰 체크 시간
	long ex_game_time;
	
	//맵 형태
	public int[][] map = {
		    {1,1,1,1,1,1,1,1,1,1},
		    {1,0,0,0,0,0,0,0,0,1},
		    {1,0,0,0,0,0,0,0,0,1},
		    {1,0,0,0,0,0,0,0,0,1},
		    {1,0,0,0,0,0,0,0,0,1},
		    {1,0,0,0,0,0,0,0,0,1},
		    {1,0,0,0,0,0,0,0,0,1},
		    {1,0,0,0,0,0,0,0,0,1},
		    {1,0,0,0,0,0,0,0,0,1},
		    {1,1,1,1,1,1,1,1,1,1},
	};
	
	//target path
	double[][] path0 = {{5,2}, {7,3}, {8,5}, {7,7},{5,8}, {3,7}, {2,5}, {3,3}};
	double[][] path1 = {{5,1}, {8,5}, {5,8}, {1,5}};
	double[][] path2 = {{2,5}, {5,2}, {8,5}, {5,8}};
	double[][] path3 = {{2,2}, {5,2}, {8,2}, {8,8}, {5,8}, {2,8}};
	double[][] path4 = {{2,2}, {8,2}, {2,8}, {8,8}};
	
	
	public MainFrame(GameFrameSettings settings) { super(settings); }
	
	/**
	 * 초기 세팅을 해주는 메서드
	 */
	@Override
	public boolean Initialize() {
	    StateSetting();
	    inputsSetting();
	    LayerSetting();
	    UserSetting();
	    TargetSetting();
	    ETCSetting();
	    return true;
	}
	
	
	
	/*
	 * Initialize 관련 메서드들
	 * ===================================================================
	 */
	
	private void StateSetting() { state = 0;}
	
	private void inputsSetting() {
	    
	    inputs.BindKey(KeyEvent.VK_W, 0);
	    inputs.BindKey(KeyEvent.VK_S, 1);
	    inputs.BindKey(KeyEvent.VK_A, 2);
	    inputs.BindKey(KeyEvent.VK_D, 3);
	    inputs.BindKey(KeyEvent.VK_C, 4);
	    inputs.BindMouseButton(MouseEvent.BUTTON3,5);
	    inputs.BindKey(KeyEvent.VK_SPACE, 6);
	    inputs.BindKey(KeyEvent.VK_ESCAPE, 7);

	    images.LoadImage("Images/target.png", "target");
	    images.LoadImage("Images/user_setting2.png", "user_setting2");
	    images.LoadImage("Images/shoot_target.png", "shoot_target");
	    images.LoadImage("Images/layer_start.png","layer_start");
	    images.LoadImage("Images/layer_end.png","layer_end");
	    images.LoadImage("Images/layer_pause.png","layer_pause");
	    images.LoadImage("Images/IMG_1042.png","lee1");
	    images.LoadImage("Images/IMG_1043.png","lee2");
	    images.LoadImage("Images/lee_start.png","lee_start");
	    images.LoadImage("Images/lee_end.png","lee_end");
	}
	
	private void LayerSetting() {
		layer_start = new Layer(0, 0, settings.canvas_width, settings.canvas_height);
		BgImage start_bg = new BgImage("lee_start");
		layer_start.children.add(start_bg);
		
		start_tb = new TextBox();
		start_tb.width = 500;
		start_tb.height = 50;
		start_tb.x = settings.canvas_width / 2 - 250;
		start_tb.y = settings.canvas_height - 150;
		start_tb.text = "Press SPACE to Start";
		start_tb.font = new Font("Arial", Font.BOLD, 40);
		start_tb.foreground_color = Color.WHITE;
		start_tb.background_color = new Color(0,0,0,0);
		
		title_tb = new TextBox();
		title_tb.width = 1000;
		title_tb.height = 150;
		title_tb.x = settings.canvas_width / 2 - 500;
		title_tb.y = settings.canvas_height / 3;
		title_tb.text = "아임이진한";
		title_tb.font = new Font("Arial", Font.BOLD, 80);
		title_tb.foreground_color = Color.WHITE;
		title_tb.background_color = new Color(0, 0, 0, 0);
		title_tb.margin_left = 0;
		
		layer_end = new Layer(0, 0, settings.canvas_width, settings.canvas_height);
		layer_end.trigger_hide = true; 
		BgImage end_bg = new BgImage("lee_end");
		layer_end.children.add(end_bg);
		
		end_tb = new TextBox();
		end_tb.width = 900;
		end_tb.height = 100;
		end_tb.x = settings.canvas_width / 2 - 450;
		end_tb.y = settings.canvas_height - 180;
		end_tb.text = "SPACE : Restart    ESC : Exit";
		end_tb.font = new Font("Arial", Font.BOLD, 35);
		end_tb.foreground_color = Color.WHITE;
		end_tb.background_color = new Color(0,0,0,0);
		
		layer_pause = new Layer(0, 0, settings.canvas_width, settings.canvas_height);
		layer_pause.trigger_hide = true; 
		
		layer_minimap = new Layer(0, 0, settings.canvas_height / 4, settings.canvas_height / 4);
		
	}
	
	private void UserSetting() { us = new User(); }

	private void TargetSetting() {
	    targets = new ArrayList<>();
	    init_target_number = 2;
	    
	    for(int i = 0 ; i < init_target_number ; i++) {
	        Target tg = new Target();

	        if(i == 0) tg.isChaser = true;

	        targets.add(tg);
	    }
	}
	
	private void ETCSetting() {
		shoot_rate = 0.99d;
		fail_rate = 0.3;
		    
		shake_power = 50;
		shake_duration = 120;
		    
		length_to_wall = new double[settings.canvas_width];
		
		minimap_scale = settings.canvas_height / 40.0;
		
		init_time = System.currentTimeMillis();
		game_time = System.currentTimeMillis();;
		check_time = System.currentTimeMillis();;
		ex_game_time = 0;
		
		plane_x = 0.66;
		plane_y = 0.0;
		
		pos_x = init_pos_x;
		pos_y = init_pos_y;
		dir_x = init_dir_x;
		dir_y = init_dir_y;
	}
	
	
	/**
	 * Update 프로세스
	 */
	
	@Override
	public boolean Update(long timeStamp) {
		inputs.AcceptInputs();
		
		ResetInputs();
		
		if (state == 0) {
			Processinputs0();
		} else if(state == 1) {
			FailCheck();
			Processinputs1();
			LevelUpTarget();
			for(Target tg : targets){
			    if(tg.isChaser) MoveTarget1(tg);
			    else MoveTarget2(tg);
			}
			SetTextBox();
		} else if(state == 2){
			Processinputs2();
		} else {
			Processinputs3();
		}
		
		return true;
	}
	
	/*
	 * Update 관련 메서드들
	 * ===================================================================
	 */
	
	/**
	 * 키 입력을 초기화.
	 */
	private void ResetInputs() {
		if(!canvas.hasFocus()) {
		    for(int i = 0; i < inputs.buttons.length; i++) {
		        inputs.buttons[i].isPressed = false;
		    }
		}
	}
	
	/**
	 * 준비 상태에서 입력을 받는 메서드
	 * 스페이스를 누르면 게임 시작
	 */
	private void Processinputs0() {
		if (inputs.buttons[6].isPressed) {
	        state = 1;
	        layer_start.trigger_hide = true;
	    }
	}
	
	/**
	 * 게임 진행 상태에서 입력을 받는 메서드
	 * w:앞 이동 a: 왼쪽 이동 s: 뒤 이동 d:오른쪽 이동
	 * 우클릭: 사격
	 * esc: pause
	 */
	private void Processinputs1() {
	    double mov_speed = 0.05;
	    double rot_speed  = 0.03;

	    // 앞뒤 이동
	    if (inputs.buttons[0].isPressed) {
	        if (map[(int) pos_y][(int)(pos_x + dir_x * mov_speed)] == 0) pos_x += dir_x * mov_speed;
	        if (map[(int)(pos_y + dir_y * mov_speed)][(int) pos_x] == 0) pos_y += dir_y * mov_speed;
	        //am.Play("footsteps");
	    }
	    if (inputs.buttons[1].isPressed) {
	        if (map[(int) pos_y][(int)(pos_x - dir_x * mov_speed)] == 0) pos_x -= dir_x * mov_speed;
	        if (map[(int)(pos_y - dir_y * mov_speed)][(int) pos_x] == 0) pos_y -= dir_y * mov_speed;
	        //am.Play("footsteps");
	    }

	    // 좌우 이동
	    if (inputs.buttons[3].isPressed) {
	        if (map[(int) pos_y][(int)(pos_x - dir_y * mov_speed)] == 0) pos_x -= dir_y * mov_speed;
	        if (map[(int)(pos_y + dir_x * mov_speed)][(int) pos_x] == 0) pos_y += dir_x * mov_speed;
	        //am.Play("footsteps");
	    }
	    if (inputs.buttons[2].isPressed) {
	        if (map[(int) pos_y][(int)(pos_x + dir_y * mov_speed)] == 0) pos_x += dir_y * mov_speed;
	        if (map[(int)(pos_y - dir_x * mov_speed)][(int) pos_x] == 0) pos_y -= dir_x * mov_speed;
	       // am.Play("footsteps");
	    }

	    
	    //마우스 이동
	    if (inputs.pos_mouseCursor.getX() >= settings.canvas_width*0.8) {
	        double old_dir_x = dir_x;
	        dir_x   =  dir_x   * Math.cos(rot_speed) - dir_y   * Math.sin(rot_speed);
	        dir_y   =  old_dir_x * Math.sin(rot_speed) + dir_y  * Math.cos(rot_speed);
	        double old_plane_x = plane_x;
	        plane_x =  plane_x * Math.cos(rot_speed) - plane_y * Math.sin(rot_speed);
	        plane_y =  old_plane_x * Math.sin(rot_speed) + plane_y * Math.cos(rot_speed);
	    }
	    if (inputs.pos_mouseCursor.getX() <= settings.canvas_width*0.2) {
	        double old_dir_x = dir_x;
	        dir_x   =  dir_x   * Math.cos(-rot_speed) - dir_y   * Math.sin(-rot_speed);
	        dir_y   =  old_dir_x * Math.sin(-rot_speed) + dir_y  * Math.cos(-rot_speed);
	        double old_plane_x = plane_x;
	        plane_x =  plane_x * Math.cos(-rot_speed) - plane_y * Math.sin(-rot_speed);
	        plane_y =  old_plane_x * Math.sin(-rot_speed) + plane_y * Math.cos(-rot_speed);
	    }
	    
	    //esc를 통해 일시정지
	    if (inputs.buttons[7].IsPressedNow()) {
	    	layer_pause.trigger_hide = false;
	    	state = 3;
	    }
	    
	    //c를 통해 치트모드
	    if (inputs.buttons[4].IsPressedNow()) {
	        targets.clear();
	        Target.ResetTarget();
	    }
	    
	    //우클릭으로 총 발사
	    if(inputs.buttons[5].IsPressedNow()) {
	    	Target best_target = Shoot();
			//if(shake_end_time < System.currentTimeMillis()) return;
	    	if(best_target != null) {
	    	    best_target.shoot_time = System.currentTimeMillis() + 3000;
	    	}
	    }
	}

	/**
	 *종료 상태에서 입력을 받는 메서드
	 *스페이스: 재시작
	 *esc: 게임 종료
	 */
	private void Processinputs2() {
		if (inputs.buttons[6].isPressed) {
	        state = 0;
	        layer_end.trigger_hide = true;
	        layer_start.trigger_hide = false;
	        ResetSetting();
	    } else if(inputs.buttons[7].isPressed) {
	    	System.exit(0);
	    }	
	}

	/**
	 * pause 상태에서 입력을 받는 메서드
	 * esc: 게임 재시작
	 */
	private void Processinputs3() {
		if (inputs.buttons[7].IsPressedNow()) {
	    	layer_pause.trigger_hide = true;
	    	state = 1;
	    }
	}

	/**
	 * 사용자 방향으로 target이 움직이도록 동작하는 매서드
	 * @param tg
	 */
	private void MoveTarget1(Target tg) {
	    if(tg.is_shoot && System.currentTimeMillis() >= tg.shoot_time) {
	        tg.is_shoot = false;
	    }

	    if(tg.is_shoot) return;

	    double dx = pos_x - tg.target_pos_x;
	    double dy = pos_y - tg.target_pos_y;

	    double dist = Math.sqrt(dx * dx + dy * dy);

	    if(dist == 0) return;

	    dx /= dist;
	    dy /= dist;

	    tg.target_pos_x += dx * tg.target_speed;
	    tg.target_pos_y += dy * tg.target_speed;
	}
	
	private void MoveTarget2(Target tg){
		if(tg.is_shoot && System.currentTimeMillis() >= tg.shoot_time) {
		    tg.is_shoot = false;
		}

		if(tg.is_shoot) return;

	    double[][] path = GetPath(tg.moveType);

	    double tx = path[tg.currentWaypoint][0];
	    double ty = path[tg.currentWaypoint][1];

	    double dx = tx - tg.target_pos_x;
	    double dy = ty - tg.target_pos_y;

	    double dist = Math.sqrt(dx * dx + dy * dy);

	    if(dist < 0.2){

	        tg.currentWaypoint++;

	        if(tg.currentWaypoint >= path.length)
	            tg.currentWaypoint = 0;

	        return;
	    }

	    dx /= dist;
	    dy /= dist;

	    tg.target_pos_x += dx * tg.target_speed;
	    tg.target_pos_y += dy * tg.target_speed;
	}
	
	/**
	 * 10초마다 타겟을 추가해주는 메서드
	 */
	private void LevelUpTarget() {
		if(System.currentTimeMillis() - check_time > 10000) {
			targets.add(new Target());
			check_time = System.currentTimeMillis();
			for(Target tg : targets) {
				tg.target_speed *= 1.1;
			}
		}
	}

	/**
	 * 게이머가 죽었는지 체크하는 메서드
	 * fail_rate: 거리에 따른 민감
	 */
	private void FailCheck() {
		for(Target tg : targets) {
			if(tg.GetDist() <fail_rate)
				state = 2;
		}
	}
	
	/**
	 * 게임 시간을 띄워주는 메서드
	 */
	private void SetTextBox() {

		game_time = System.currentTimeMillis() - init_time;

		long minute = game_time / 60000;
		long second = (game_time % 60000) / 1000;
		long milli  = (game_time % 1000);

		us.user_tb.text = String.format("%02d:%02d:%03d", minute, second, milli);
	}
	
	/**
	 * 게임을 재시작할 때 타겟을 초기화시키는 메서드
	 */
	private void ResetSetting() {

		init_time = System.currentTimeMillis();
		check_time = System.currentTimeMillis();
		game_time = 0;
		
		plane_x = 0.66;
		plane_y = 0.0;

		shake_end_time = 0;

		pos_x = init_pos_x;
		pos_y = init_pos_y;
		dir_x = init_dir_x;
		dir_y = init_dir_y;

		targets.clear();
		Target.ResetTarget();
		
		for(int i = 0; i < init_target_number; i++) {
			targets.add(new Target());
		}

	}


	/**
	 * Draw 프로세스
	 */
	@Override
	public void Draw(long timeStamp) {
		BeginDraw();
	    ClearScreen();

	    if(state == 0) {
	        layer_start.Draw(g);
	        title_tb.Draw(g);
	        start_tb.Draw(g);
	    } else if(state == 1) {
	        Shake();
	        DrawBackground();
	        RayCasting();
	        DrawTargets();
	        DrawUserSetting();
	        DrawMiniMap();
	    } else if(state == 2) {
	        layer_end.Draw(g);
	        end_tb.Draw(g);
	    } else if(state == 3) {
	    	 DrawBackground();
	         RayCasting();
	         DrawTargets();
	         DrawUserSetting();
	         DrawMiniMap();
	         DrawPause();
	    }
	    
	    EndDraw();
	}
	/*
	 * Draw 관련 메서드들
	 * ===================================================================
	 */
	
	/**
	 *화면을 흔들리게 하는 효과
	 *shake_power: 흔들리는 픽셀 범위의 크기
	 *shake_duration: 흔들리는 효과의 지속시간
	 */
	private void Shake() {
		
		 int shake_x = 0;
		 int shake_y = 0;
		    
		 if(System.currentTimeMillis() < shake_end_time) {
		        shake_x = (int)((Math.random() - 0.5) * shake_power);
		        shake_y = (int)((Math.random() - 0.5) * shake_power);
		 }

		 g.translate(shake_x, shake_y);
	}
	
	/**
	 *배경화면 색을 칠해주는 메서드
	 */
	private void DrawBackground() {
		 
		 SetColor(new Color(20, 24, 28));
	     g.fillRect(0, 0, settings.canvas_width, settings.canvas_height / 2);

	     SetColor(new Color(38, 42, 46));
	     g.fillRect(0, settings.canvas_height / 2, settings.canvas_width, settings.canvas_height / 2);
	}
	
	/**
	 * 레이캐스팅 해주는 메서드
	 */
	private void RayCasting() {
		
		 for (int i = 0; i < settings.canvas_width; i++) {
			int map_x = (int) pos_x;
	        int map_y = (int) pos_y;
	            
	        //광선벡터
	        double ray_dir_x = dir_x + plane_x * ( 2.0 * i / settings.canvas_width - 1.0 );
	        double ray_dir_y = dir_y + plane_y * ( 2.0 * i / settings.canvas_width - 1.0 );
			
			double delta_dist_x = Math.abs(1.0 / ray_dir_x);
	        double delta_dist_y = Math.abs(1.0 / ray_dir_y);
	        
            double side_dist_x, side_dist_y;
            int step_x, step_y;

            if (ray_dir_x < 0) { 
            	step_x = -1; 
            	side_dist_x = (pos_x - map_x) * delta_dist_x; 
            } else { 
            	step_x =  1; 
            	side_dist_x = (map_x + 1.0 - pos_x) * delta_dist_x; 
            }
            
            if (ray_dir_y < 0) { 
            	step_y = -1; 
            	side_dist_y = (pos_y - map_y) * delta_dist_y; 
            } else { 
            	step_y =  1; 
            	side_dist_y = (map_y + 1.0 - pos_y) * delta_dist_y; 
            }

            int side = 0;
            
            while (true) {
                if (side_dist_x < side_dist_y) { 
                	side_dist_x += delta_dist_x; 
                	map_x += step_x;
                	side = 0;
                } else { 
                	side_dist_y += delta_dist_y; 
                	map_y += step_y; 
                	side = 1; 
                }
                
                if (map[map_y][map_x] == 1) break;
            }

            double perp_wall_dist = (side == 0)
                ? (map_x - pos_x + (1 - step_x) / 2.0) / ray_dir_x
                : (map_y - pos_y + (1 - step_y) / 2.0) / ray_dir_y;

            int draw_start = Math.max(0, settings.canvas_height / 2 - (int) (settings.canvas_height / perp_wall_dist) / 2);
            int draw_end   = Math.min(settings.canvas_height - 1, settings.canvas_height / 2 + (int) (settings.canvas_height / perp_wall_dist) / 2);
	        
            Color wallColor = (side == 1)
            	? new Color(65, 72, 80)
                : new Color(95, 105, 115);

            SetColor(wallColor);
            g.drawLine(i, draw_start, i, draw_end);
            
            length_to_wall[i] = perp_wall_dist;
		}
	}
	
	/**
	 * targets을 그려주는 메서드
	 */
	private void DrawTargets() {
	    if(targets == null) return;
	    if(targets.isEmpty()) return;

	    SortTargets(0, targets.size()-1);
	    
	    for(Target tg : targets)
	        tg.Draw(g);
	}
	
	/**
	 * User을 그려주는 메서드
	 */
	private void DrawUserSetting() { us.Draw(g); }
	
	/**
	 * 미니맵을 그려주는 메서드
	 */
	private void DrawMiniMap() {
	    for(int y = 0; y < map.length; y++) {
	        for(int x = 0; x < map[0].length; x++) {

	            if(map[y][x] == 1)
	                SetColor(Color.BLACK); // 벽
	            else
	                SetColor(Color.WHITE); // 바닥

	            g.fillRect(x * settings.canvas_height/40, y * settings.canvas_height/40, settings.canvas_height/40, settings.canvas_height/40 );
	        }
	    }
	    
	    int user_size = 20;
	    SetColor(Color.BLUE);
	    
	    g.fillOval((int)(pos_x * settings.canvas_height/40) - user_size/2, (int)(pos_y * settings.canvas_height/40) - user_size/2, user_size, user_size);
	    
	    int target_size = 15;

	    for(Target tg : targets) {
	    	if(tg.getIsChaser()) SetColor(Color.GREEN);
	    	else SetColor(Color.RED);
	    	g.fillOval((int)(tg.target_pos_x * settings.canvas_height/40) - target_size/2, (int)(tg.target_pos_y * settings.canvas_height/40) - target_size/2, target_size, target_size);
	    }
	}
	        

	/**
	 * pause상태 창을 띄워주는 메서드
	 */
	private void DrawPause() {

	    SetColor(new Color(0, 0, 0, 150));
	    g.fillRect(0, 0, settings.canvas_width, settings.canvas_height);

	    SetColor(Color.WHITE);
	    SetFont("Arial BOLD 100");

	    DrawString(
	        settings.canvas_width / 2 - g.getFontMetrics().stringWidth("PAUSED") / 2,
	        settings.canvas_height / 2,
	        "PAUSED"
	    );
	}	   
	
	
	/**
	 * 사용자 관련 요소들을 담은 객체
	 */
	private class User extends DrawableObject{
		
		private TextBox user_tb;
		
		public User() {
			user_tb = new TextBox();

		    user_tb.width = 300;
		    user_tb.height = 50;

		    user_tb.x = settings.canvas_width / 2 - user_tb.width / 2;
		    user_tb.y = 10;
		    user_tb.margin_left = 0;

		    user_tb.text = "00:00:000";
		    user_tb.font = new Font("Arial", Font.BOLD, 30);

		    user_tb.foreground_color = Color.WHITE;
		    user_tb.background_color = new Color(0,0,0,0);
		}
		
		@Override
		public void Draw(Graphics2D g) {
			g.drawImage(images.GetImage("user_setting2"), 0, 0, settings.canvas_width, settings.canvas_height, null); //손모양
			//g.drawImage(image, x, y, x, y, x, y, x, y, rootPane);
			user_tb.Draw(g);
		}
	}
	
	
	
	/**
	 * target 객체
	 */
	private class Target extends DrawableObject{
		
		static int target_num = 0;
		
		private int num;
		private double target_pos_x;
		private double target_pos_y;
		private double target_speed;
		private boolean is_shoot;
		private long shoot_time;
		private int moveType;
		private int currentWaypoint;
		private boolean isChaser;
		
		
		public Target() {
		    SetTargetPosition();
		    target_speed = 0.02;
		    is_shoot = false;
		    shoot_time = 0;
		    num = target_num;
		    target_num++;
		    if(num == 0) isChaser = true;
		    moveType = (int)(Math.random() * 5);
		    currentWaypoint = (int)(Math.random() * GetPath(moveType).length);
		}
		
		@Override
		public void Draw(Graphics2D g) {
			Image img;
			
			img = !is_shoot
			? images.GetImage("lee1")
			: images.GetImage("lee2");
			
			double sprite_x = target_pos_x - pos_x;
			double sprite_y = target_pos_y - pos_y;
			
			
			double det = 1.0 / (plane_x * dir_y - dir_x * plane_y);
			double transform_x = det * (dir_y * sprite_x - dir_x * sprite_y);
			double transform_y = det * (-plane_y * sprite_x + plane_x * sprite_y);
			
			if(transform_y <= 0) return;
			
			int screen_x = (int)((settings.canvas_width / 2) * (1 + transform_x / transform_y));
			int size = Math.abs((int)(settings.canvas_height / transform_y));
			
			int draw_start_x = screen_x - size / 2;
			int draw_end_x   = screen_x + size / 2;
			int draw_start_y = settings.canvas_height / 2 - size / 2;
			int draw_end_y = settings.canvas_height / 2 + size / 2;
			
			for(int stripe = draw_start_x; stripe < draw_end_x; stripe++) {
				if(stripe < 0 || stripe >= settings.canvas_width) continue;
				if(transform_y > length_to_wall[stripe]) continue;
				
				int tx = (int)((stripe - draw_start_x) * img.getWidth(null)/size);
				
				g.drawImage(img, stripe, draw_start_y, stripe+1, draw_end_y, tx, 0, tx+1, img.getHeight(null), null);
			}
		}
		
		public double GetDist() {
		    double dx = target_pos_x - pos_x;
		    double dy = target_pos_y - pos_y;
		    return Math.sqrt(dx * dx + dy * dy);
		}
		
		public static void ResetTarget() { target_num = 0; }
		
		public boolean getIsChaser() {
			return isChaser;
		}
		private void SetTargetPosition() {
			target_pos_x = 8 * Math.random()+1;
			target_pos_y = 8 * Math.random()+1;
			
			while(true){
				if(GetDist() <fail_rate) {
					target_pos_x = 8 * Math.random()+1;
					target_pos_y = 8 * Math.random()+1;
				} else break;
			}
		}
	}
	
	/**
	 * layer 배경화면을 그리기 위한 객체
	 */
	public class BgImage extends DrawableObject{
		String bg_image;
		
		public BgImage(String bg_image) {
			this.bg_image = bg_image;
		}
		
		@Override
		public void Draw(Graphics2D g) {
			g.drawImage(images.GetImage(bg_image), 0, 0, settings.canvas_width, settings.canvas_height, null); //손모양
			//g.drawImage(image, x, y, x, y, x, y, x, y, rootPane);
		}
	}
	
	
	/*
	 * 여기부턴 도구적인 함수
	 * =========================================================================================
	 */
	
	private Target Shoot() {

		System.out.println("Shoot"); 
		
	    Target best_target = null;
	    double best_dist = Double.MAX_VALUE;

	    for(Target tg : targets) {

	        double dx = tg.target_pos_x - pos_x;
	        double dy = tg.target_pos_y - pos_y;

	        double dist = tg.GetDist();

	        dx /= dist;
	        dy /= dist;

	        double dot = dx * dir_x + dy * dir_y;
	        System.out.println(dot);
	        
	        if(dot > shoot_rate) {
	            if(dist < best_dist) {
	                best_dist = dist;
	                best_target = tg;
	            }
	        }
	    }

	    if(best_target != null && best_target.is_shoot == false) {
	        best_target.is_shoot = true;
	        System.out.println("HIT "+best_target.num);
	        
	    } else System.out.println("MISS");
	    

	    shake_end_time = System.currentTimeMillis() + shake_duration;

	    return best_target;
	}
	
	
	private void SortTargets(int left, int right) {

	    int i = left;
	    int j = right;

	    double pivot_dist = targets.get((left + right) / 2).GetDist();

	    while(i <= j) {
	        while(targets.get(i).GetDist() > pivot_dist) i++;
	        while(targets.get(j).GetDist() < pivot_dist) j--;
	        
	        if(i <= j) {
	            Target temp = targets.get(i);
	            targets.set(i, targets.get(j));
	            targets.set(j, temp);

	            i++;
	            j--;
	        }
	    }

	    if(left < j) SortTargets(left, j);
	    if(i < right) SortTargets(i, right);
	}
	
	private double[][] GetPath(int moveType){
	    switch(moveType){
	        case 0:
	            return path0;
	        case 1:
	            return path1;
	        case 2:
	            return path2;
	        case 3:
	            return path3;
	        default:
	            return path4;
	    }
	}

}
