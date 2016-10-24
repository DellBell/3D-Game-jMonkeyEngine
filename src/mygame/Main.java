package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.effect.ParticleEmitter;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl;
import com.jme3.system.AppSettings;
import com.jme3.water.WaterFilter;
import de.lessvoid.nifty.Nifty;


/**
 * @author Delshad
 */
public class Main extends SimpleApplication implements AnimEventListener, PhysicsCollisionListener, PhysicsTickListener, ActionListener
{
    StartScreen startScreen;
    DirectionalLight sun;
    World world = new World();
    float moveSpeed = 2f;
    private AudioNode globalStartMusic, globalIngameMusic, globalKickAudio, 
            globalPunchAudio, globalRound, globalR1, globalR2, globalFinal, 
            globalIngameMusic2, globalFight, globalWindAudio;
    private AudioNode ninjaBackwardAudio, ninjaJumpAudio, ninjaDieAudio,
            ninjaPunchAudio, ninjaKickAudio, ninjaPunchHitAudio, ninjaKickHitAudio;
    private AudioNode enemyBackwardAudio, enemyJumpAudio, enemyDieAudio,
            enemyPunchAudio, enemyKickAudio, enemyKickHitAudio, enemyPunchHitAudio;
    private int switchCounterLeft = 0;
    private int switchCounterRight = 1;
    private AnimChannel ninjaAnimChannel;
    private AnimChannel enemyAnimChannel;
    private AnimControl ninjaAnimControl;
    private AnimControl enemyAnimControl;
    private int enemyWins = 0;
    private int ninjaWins = 0;
    private int gameRound = 1;
    private float ATK_DMG = 5f;
    private float KICK_DMG = 10f;
    private float camNodePosX;
    private float time = 0;
    private float timeLeft = 60;
    private float gameFinishCountDown = 5f;
    private float roundFinishCountDown = 3f;
    private float HEALTH = 100;
    private BulletAppState bulletAppState;
    private CameraNode camNode;
    private ChaseCamera chaseCamera;
    private Node camControlNode;
    private Vector3f ninjaUpDir, ninjaRightDir, enemyDownDir, enemyLeftDir;
    private Vector3f camNodeStartPos = new Vector3f(camNodePosX, 3.5f, 9);
    private Vector3f ninjaWalkDirection = new Vector3f(0, 0, 0);
    private Vector3f ninjaViewDirection = new Vector3f(1, 0, 0);
    private Vector3f enemyWalkDirection = new Vector3f(0, 0, 0);
    private Vector3f enemyViewDirection = new Vector3f(-1, 0, 0);
    private WaterFilter water;
    private ParticleEmitter pEmitter;
    private Node worldNode = new Node("mainWorldNode");
    private Node guiSubNode = new Node("GuiSubNode");
    private Node sfxNode = new Node("sfxNode");
    protected BitmapText guiGameTime,guiWinsText,guiGameRound,guiFight,
                         guiEnemyWins,guiNinjaWins,guiPlayerNinja,
                         guiPlayerEnemy;
    boolean ninjaMoveLeft = false, ninjaMoveRight = false, ninjaMoveUp = false, 
            ninjaMoveDown = false, ninjaKick = false, ninjaAtk = false,
            enemyMoveLeft = false, enemyMoveRight = false, enemyMoveUp = false, 
            enemyMoveDown = false, enemyKick = false, enemyAtk = false,
            enemyDie = false, ninjaDie = false, enableKeys = false, 
            gameFinish = false, exitGame = false, Switch = false,
            switchDone = false,
            change = false;
    private String NinjaMoveLeft = "NinjaMoveLeft", 
            NinjaMoveRight = "NinjaMoveRight",
            NinjaMoveUp = "NinjaMoveUp", 
            NinjaMoveDown = "NinjaMoveDown",
            NinjaJump = "NinjaJump",
            NinjaKick = "NinjaKick",
            NinjaATK = "NinjaAttack",
            EnemyMoveLeft = "EnemyMoveLeft", 
            EnemyMoveRight = "EnemyMoveRight",
            EnemyMoveUp = "EnemyaMoveUp", 
            EnemyMoveDown = "EnemyMoveDown",
            EnemyJump = "EnemyJump",
            EnemyKick = "EnemyKick",
            EnemyATK = "EnemyAttack",
            Jump = "Jump",
            Kick = "Kick",
            ATK = "Attack3",
            Walk = "Walk",
            Backflip = "Backflip",
            ExitGame = "ExitGame",
            ResetGame = "ResetGame",
            strHealth = "health",
            hp = "",
            ChangeMusic = "ChangeIngameMusic";
    
    public static void main(String[] args) 
    {
        AppSettings settings = new AppSettings(true);
        settings.setVSync(true);
        Main app = new Main();
        settings.put("Title", "Tekken 7: The Ultimate JME3 Edition!");
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() 
    {
        setDisplayFps(false);
        setDisplayStatView(false);
        
        startScreen = new StartScreen();
        stateManager.attach(startScreen);

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);
        nifty.fromXml("Interface/ScreenOne.xml", "start", startScreen);
        
        initWorld();
        initKeys();
        initAnimation();
        addFollowCam();
        addGui();
        addGlobalSound();
        addNinjaSound();
        addEnemySound();
        
        rootNode.attachChild(sfxNode);
        rootNode.attachChild(worldNode);
    }
    private void initAnimation()
    {
        ninjaAnimControl = world.getNinja().getControl(AnimControl.class);
        enemyAnimControl = world.getEnemy().getControl(AnimControl.class);
        ninjaAnimControl.addListener(this);
        enemyAnimControl.addListener(this);
        ninjaAnimChannel = ninjaAnimControl.createChannel();
        enemyAnimChannel = enemyAnimControl.createChannel();
        ninjaAnimChannel.setAnim("Idle1",0.05f);
        enemyAnimChannel.setAnim("Idle1",0.05f);
    }
    
    private void initWorld() 
    {
        createPhysics();
        worldNode = world.createTerrain(this);
        water = world.initPPcWater(worldNode, this);
        worldNode = world.createFloor(bulletAppState);
        
        worldNode = world.createNinja(bulletAppState);
        world.getNinjaNode().setUserData(strHealth, HEALTH);
        
        worldNode = world.createEnemy(bulletAppState);
        world.getEnemyNode().setUserData(strHealth, HEALTH);
        
        pEmitter = world.prepareEffect();
        worldNode.attachChild(pEmitter);
        addColdLight();
    }
    
    private void createPhysics()
    {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        getPhysicsSpace().addCollisionListener(this);
//        getPhysicsSpace().enableDebug(assetManager); 
    }
    
    //Cold environment
    private void addColdLight() 
    {
        // Set up ambient light.
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.Cyan);
        rootNode.addLight(ambient);
        
        DirectionalLight sun2 = new DirectionalLight();
        sun2.setDirection(new Vector3f(0.5f, 0f, 0.5f).normalizeLocal());
        sun2.setColor(ColorRGBA.Cyan.mult(0.3f));
        world.getNinjaNode().addLight(sun2);
        
        DirectionalLight sun3 = new DirectionalLight();
        sun3.setDirection(new Vector3f(0.5f, 0f, 0.5f).normalizeLocal());
        sun3.setColor(ColorRGBA.Cyan.mult(0.3f));
        world.getEnemyNode().addLight(sun3);
        
        /** A white, directional light source */ 
        sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0, -1, -1).normalizeLocal());
        sun.setColor(ColorRGBA.Cyan);
        rootNode.addLight(sun);
    }
    
    private void addGlobalSound()
    {
        try {
            globalStartMusic = new AudioNode(assetManager, "Sounds/global/startMusic1CH.wav", false);
        } catch (Exception e) {
            System.out.println("In catch");
            e.getCause();
        }
        globalStartMusic.setLooping(true);
        globalStartMusic.setVolume(1.0f);
        globalStartMusic.setReverbEnabled(false);
        globalStartMusic.setPositional(false); 
        globalStartMusic.play();
        
        globalWindAudio = new AudioNode(assetManager, "Sounds/global/wind.wav", false);
        globalWindAudio.setLooping(true);
        globalWindAudio.setVolume(1.5f);
        globalWindAudio.setPositional(false);
        
        globalIngameMusic = new AudioNode(assetManager, "Sounds/global/juliaArcade.wav", false);
        globalIngameMusic.setLooping(true);
        globalIngameMusic.setVolume(1.0f);
        globalIngameMusic.setPositional(false);
        globalIngameMusic.setReverbEnabled(false);
        
        globalIngameMusic2 = new AudioNode(assetManager, "Sounds/global/T3Anna2CH.wav", false);
        globalIngameMusic2.setLooping(false);
        globalIngameMusic2.setVolume(2.0f);
        globalIngameMusic2.setPositional(false);
        
        globalPunchAudio = new AudioNode(assetManager, "Sounds/global/punch.wav", false);
        globalPunchAudio.setLooping(false);
        globalPunchAudio.setVolume(2.0f);
        globalPunchAudio.setPositional(false);
        
        globalKickAudio = new AudioNode(assetManager, "Sounds/global/kick.wav", false);
        globalKickAudio.setLooping(false);
        globalKickAudio.setVolume(2.0f);
        globalKickAudio.setPositional(false);
        
        globalFight = new AudioNode(assetManager, "Sounds/global/fight.wav", false);
        globalFight.setLooping(false);
        globalFight.setVolume(2.0f);
        globalFight.setPositional(false);
        
        globalRound = new AudioNode(assetManager, "Sounds/global/round.wav", false);
        globalRound.setLooping(false);
        globalRound.setVolume(2.0f);
        globalRound.setPositional(false);
        
        globalR1 = new AudioNode(assetManager, "Sounds/global/1.wav", false);
        globalR1.setLooping(false);
        globalR1.setVolume(2.0f);
        globalR1.setPositional(false);
        
        globalR2 = new AudioNode(assetManager, "Sounds/global/2.wav", false);
        globalR2.setLooping(false);
        globalR2.setVolume(2.0f);
        globalR2.setPositional(false);
        
        globalFinal = new AudioNode(assetManager, "Sounds/global/final.wav", false);
        globalFinal.setLooping(false);
        globalFinal.setVolume(2.0f);
        globalFinal.setPositional(false);
        
        sfxNode.attachChild(globalIngameMusic2);
        sfxNode.attachChild(globalR2);
        sfxNode.attachChild(globalR1);
        sfxNode.attachChild(globalRound);
        sfxNode.attachChild(globalFight);
        sfxNode.attachChild(globalWindAudio);
        sfxNode.attachChild(globalFinal);
        sfxNode.attachChild(globalKickAudio);
        sfxNode.attachChild(globalStartMusic);
        sfxNode.attachChild(globalIngameMusic);
        sfxNode.attachChild(globalPunchAudio);
    }
    
    private void addNinjaSound()
    {
        ninjaJumpAudio = new AudioNode(assetManager, "Sounds/ninja/ninjaJump.wav", false);
        ninjaJumpAudio.setLooping(false);
        ninjaJumpAudio.setVolume(2.0f);
        ninjaJumpAudio.setPositional(false);
        
        ninjaBackwardAudio = new AudioNode(assetManager, "Sounds/ninja/ninjaBW.wav", false);
        ninjaBackwardAudio.setLooping(false);
        ninjaBackwardAudio.setVolume(2.0f);
        ninjaBackwardAudio.setPositional(false);
        
        ninjaDieAudio = new AudioNode(assetManager, "Sounds/ninja/ninjaDie.wav", false);
        ninjaDieAudio.setLooping(false);
        ninjaDieAudio.setVolume(2.0f);
        ninjaDieAudio.setPositional(true);
        ninjaDieAudio.setReverbEnabled(true);
        
        ninjaPunchHitAudio = new AudioNode(assetManager, "Sounds/ninja/ninjaPunchHit.wav", false);
        ninjaPunchHitAudio.setLooping(false);
        ninjaPunchHitAudio.setVolume(2.0f);
        ninjaPunchHitAudio.setPositional(false);
        
        ninjaKickHitAudio = new AudioNode(assetManager, "Sounds/ninja/ninjaKickHit.wav", false);
        ninjaKickHitAudio.setLooping(false);
        ninjaKickHitAudio.setVolume(2.0f);
        ninjaKickHitAudio.setPositional(false);
        
        ninjaKickAudio = new AudioNode(assetManager, "Sounds/ninja/ninjaKick.wav", false);
        ninjaKickAudio.setLooping(false);
        ninjaKickAudio.setVolume(2.0f);
        ninjaKickAudio.setPositional(false);
        
        ninjaPunchAudio = new AudioNode(assetManager, "Sounds/ninja/ninjaPunch.wav", false);
        ninjaPunchAudio.setLooping(false);
        ninjaPunchAudio.setVolume(2.0f);
        ninjaPunchAudio.setPositional(false);
        
        sfxNode.attachChild(ninjaKickAudio);
        sfxNode.attachChild(ninjaPunchAudio);
        sfxNode.attachChild(ninjaKickAudio);
        sfxNode.attachChild(ninjaPunchHitAudio);
        sfxNode.attachChild(ninjaDieAudio);
        sfxNode.attachChild(ninjaBackwardAudio);
        sfxNode.attachChild(ninjaJumpAudio);
    }
    
    private void addEnemySound()
    {
        enemyJumpAudio = new AudioNode(assetManager, "Sounds/enemy/jinJump.wav", false);
        enemyJumpAudio.setLooping(false);
        enemyJumpAudio.setVolume(2.0f);
        enemyJumpAudio.setPositional(false);
        
        enemyBackwardAudio = new AudioNode(assetManager, "Sounds/enemy/jinBW.wav", false);
        enemyBackwardAudio.setLooping(false);
        enemyBackwardAudio.setVolume(2.0f);
        enemyBackwardAudio.setPositional(false);
        
        enemyDieAudio = new AudioNode(assetManager, "Sounds/enemy/jinDie.wav", false);
        enemyDieAudio.setLooping(false);
        enemyDieAudio.setVolume(2.0f);
        enemyDieAudio.setPositional(true);
        enemyDieAudio.setReverbEnabled(true);
        
        enemyKickHitAudio = new AudioNode(assetManager, "Sounds/enemy/jinKickHit.wav", false);
        enemyKickHitAudio.setLooping(false);
        enemyKickHitAudio.setVolume(2.0f);
        enemyKickHitAudio.setPositional(false);
        
        enemyPunchHitAudio = new AudioNode(assetManager, "Sounds/enemy/jinPunchHit.wav", false);
        enemyPunchHitAudio.setLooping(false);
        enemyPunchHitAudio.setVolume(2.0f);
        enemyPunchHitAudio.setPositional(false);
        
        enemyKickAudio = new AudioNode(assetManager, "Sounds/enemy/jinKick.wav", false);
        enemyKickAudio.setLooping(false);
        enemyKickAudio.setVolume(2.0f);
        enemyKickAudio.setPositional(false);
        
        enemyPunchAudio = new AudioNode(assetManager, "Sounds/enemy/jinPunch.wav", false);
        enemyPunchAudio.setLooping(false);
        enemyPunchAudio.setVolume(2.0f);
        enemyPunchAudio.setPositional(false);
        
        sfxNode.attachChild(enemyPunchAudio);
        sfxNode.attachChild(enemyKickAudio);
        sfxNode.attachChild(enemyPunchHitAudio);
        sfxNode.attachChild(enemyKickHitAudio);
        sfxNode.attachChild(enemyDieAudio);
        sfxNode.attachChild(enemyBackwardAudio);
        sfxNode.attachChild(enemyJumpAudio);
    }
    
    private void addGui()
    {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        
        guiFight = new BitmapText(guiFont, false);
        guiFight.setSize(guiFont.getCharSet().getRenderedSize()*3);
        guiFight.setColor(ColorRGBA.Red);
        
        guiGameTime = new BitmapText(guiFont, false);
        guiGameTime.setSize(guiFont.getCharSet().getRenderedSize()*1);
        guiGameTime.setLocalTranslation(settings.getWidth()/2 - 
                0.04f*settings.getWidth() , 0.97f*settings.getHeight(), 0);
        guiGameTime.setColor(ColorRGBA.Cyan);
        
        guiWinsText = new BitmapText(guiFont, false);
        guiWinsText.setSize(guiFont.getCharSet().getRenderedSize()*1);
        guiWinsText.setText("-- Wins --");
        guiWinsText.setLocalTranslation(settings.getWidth()/2 - 
                0.041f*settings.getWidth(), 0.93f*settings.getHeight(), 0);
        guiWinsText.setColor(ColorRGBA.Yellow);
        
        guiEnemyWins = new BitmapText(guiFont, false);
        guiEnemyWins.setSize(guiFont.getCharSet().getRenderedSize()*1);
        guiEnemyWins.setLocalTranslation(settings.getWidth()/2 - 
                0.09f*settings.getWidth(), 0.93f*settings.getHeight(), 0);
        guiEnemyWins.setColor(ColorRGBA.Yellow);
        
        guiNinjaWins = new BitmapText(guiFont, false);
        guiNinjaWins.setSize(guiFont.getCharSet().getRenderedSize()*1);
        guiNinjaWins.setLocalTranslation(settings.getWidth()/2 +
                0.062f*settings.getWidth(), 0.93f*settings.getHeight(), 0);
        guiNinjaWins.setColor(ColorRGBA.Yellow);
        
        guiPlayerNinja = new BitmapText(guiFont, false);
        guiPlayerNinja.setSize(guiFont.getCharSet().getRenderedSize()*1);
        guiPlayerNinja.setText("Player1");
        guiPlayerNinja.setLocalTranslation(settings.getWidth()/2 +
                0.36f*settings.getWidth(), 0.93f*settings.getHeight(), 0);
        guiPlayerNinja.setColor(ColorRGBA.Cyan);
        
        guiPlayerEnemy = new BitmapText(guiFont, false);
        guiPlayerEnemy.setSize(guiFont.getCharSet().getRenderedSize()*1);
        guiPlayerEnemy.setText("Player2");
        guiPlayerEnemy.setLocalTranslation(settings.getWidth()/2 -
                0.43f*settings.getWidth(), 0.93f*settings.getHeight(), 0);
        guiPlayerEnemy.setColor(ColorRGBA.Cyan);
        
        
        guiSubNode.attachChild(guiFight);
        guiSubNode.attachChild(guiEnemyWins);
        guiSubNode.attachChild(guiNinjaWins);
        guiSubNode.attachChild(guiGameTime);
        guiNode.attachChild(guiSubNode);
    }

    private void updateGuiSound(float tpf)
    {
        int changeText = (int) time;
        guiFight.setLocalTranslation((settings.getWidth() / 2) 
                 - (guiFight.getLineWidth()/2), settings.getHeight() / 2 
                + guiFight.getLineHeight()/2,0);
        if (time >= 3){ 
            timeRemain(tpf);
        }
        if(time < 1){
            guiSubNode.attachChild(guiPlayerNinja);
            guiSubNode.attachChild(guiPlayerEnemy);
            guiSubNode.attachChild(guiWinsText);
            guiSubNode.attachChild(guiGameTime);
        }
        if(gameRound == 1 || gameRound == 2){
            if(time < 1.4 && time > 1){
                globalRound.play();
            }
        }
        if(time <= 3.2 && time > 2.7 && gameRound <= 2 && 
                (enemyWins < 2 || ninjaWins < 2)) {
            globalFight.play();
        }
        if(changeText == 3){
            guiFight.setText("FIGHT!");
            enableKeys = true;
        } else {
            guiFight.setText("");
        }
        if (gameRound == 1 && changeText <=2 ) {
            guiFight.setText("ROUND 1");
            if(time < 2 && time > 1.6){globalR1.play();}
        }
        else if (gameRound == 2 && changeText <= 2) {
            guiFight.setText("ROUND 2");
            if(time < 2 && time > 1.6){globalR2.play();}
        }
        else if (gameRound == 3 && changeText <= 2 && 
                (enemyWins == 1 || ninjaWins == 1))
        { 
            guiFight.setText("FINAL ROUND!");
            if(time < 2.0 && time > 1.5) {
                globalFinal.play();
            }
        } 
        
        String enemy_wins = Integer.toString(enemyWins);
        guiEnemyWins.setText(enemy_wins + "/2");
        
        String ninja_wins = Integer.toString(ninjaWins);
        guiNinjaWins.setText(ninja_wins + "/2");
    }
    
    private void disableMovement()
    {
        //To avoid game ending 
        ninjaMoveLeft = false;
        ninjaMoveRight = false;
        ninjaMoveUp = false;
        ninjaMoveDown = false;
        enemyMoveLeft = false;
        enemyMoveRight = false;
        enemyMoveUp = false;
        enemyMoveDown = false;
    }
    private void checkGameState(float enemyHealth, float ninjaHealth, float tpf) 
    {
        if (timeLeft <= 0){
            if(enemyHealth < ninjaHealth){
                float currentHP = 0f;
                world.getEnemyNode().setUserData(strHealth, currentHP);
                hp = Float.toString(currentHP); 
                startScreen.getEnemyHP(hp+"%");
                
                if (enemyDie == true){
                    enemyDieAudio.playInstance();
                }
                enemyDie = false;
                enableKeys = false;
                disableMovement();
                if(roundFinishCountDown <= 0){
                    ninjaWins +=1;
                    gameRound +=1;
                    newRound();
                }
                roundFinishCountDown -= tpf;
            }

            if (ninjaHealth > 0 && enemyHealth > 0){
                enemyDie = true;
                ninjaDie = true;
            }
            
            else if (ninjaHealth < enemyHealth) {
                float currentHP = 0f;
                world.getNinjaNode().setUserData(strHealth, currentHP);
                hp = Float.toString(currentHP); 
                startScreen.getEnemyHP(hp+"%");
                if (ninjaDie == true){
                    enemyDieAudio.playInstance();
                }
                ninjaDie = false;
                enableKeys = false;
                disableMovement();
                if(roundFinishCountDown <= 0){
                    enemyWins +=1;
                    gameRound +=1;
                    newRound();
                }
                roundFinishCountDown -= tpf;
            }
            if(enemyHealth == ninjaHealth){
                ninjaWins+=1;
                enemyWins+=1;
                gameRound+=1;
                ninjaDieAudio.playInstance();
                enemyDieAudio.playInstance();
                newRound();
            }
        }
        if (ninjaWins == 2 && gameRound >= 3) {
            int blink = (int)time;
            enableKeys = false;
            guiFight.setText("PLAYER1 WINS!");
            guiSubNode.detachChild(guiGameTime);
            if (blink%2 == 0) //Blinking effects
            {
                guiSubNode.detachChild(guiFight);
            } else{
                guiSubNode.attachChild(guiFight);
            }
            if(gameFinishCountDown <= 0){
                gameFinish = true;
            }
            gameFinishCountDown -= tpf;
        }
        if(enemyHealth <=0){
            if (enemyDie) {
                enemyDieAudio.playInstance();
                enemyAnimChannel.reset(true);
                enemyAnimChannel.setAnim("Death1", 0.5f);
                enemyAnimChannel.setLoopMode(LoopMode.DontLoop);
                enemyAnimChannel.setSpeed(0.8f);
            }
            enemyDie = false;
            enableKeys = false;
            disableMovement();
            if(roundFinishCountDown <= 0) {
                ninjaWins +=1;
                gameRound +=1;
                newRound();
            }
            roundFinishCountDown -= tpf;
        }
        if (ninjaHealth > 0 && enemyHealth > 0){
            enemyDie = true;
            ninjaDie = true;
        }
        
        if (enemyWins == 2 && gameRound >= 3) {
            int blink = (int)time;
            enableKeys = false;
            guiFight.setText("PLAYER2 WINS!");
            guiSubNode.detachChild(guiGameTime);
            if (blink%2 == 0) //Blinking effects
            {
                guiSubNode.detachChild(guiFight);
            } else {
                guiSubNode.attachChild(guiFight);
            }
            if(gameFinishCountDown <= 0) {
                gameFinish = true;
            }
            gameFinishCountDown -= tpf;
        }
        if (ninjaHealth <= 0)  {
            if (ninjaDie) {
                ninjaDieAudio.playInstance();
                ninjaAnimChannel.reset(true);
                ninjaAnimChannel.setAnim("Death1", 0.5f);
                ninjaAnimChannel.setLoopMode(LoopMode.DontLoop);
                ninjaAnimChannel.setSpeed(0.8f);
            }
            ninjaDie = false;
            enableKeys = false;
            disableMovement();
            if(roundFinishCountDown <= 0) {
                enemyWins +=1;
                gameRound +=1;
                newRound();
            }
            roundFinishCountDown -= tpf;
        }
    }
    
    private void addFollowCam()
    {
        flyCam.setEnabled(false);
        //init camera control node
        camControlNode = new Node("camControlNode");
        camControlNode.setLocalTranslation(camNodeStartPos);
       
        //cube to see middle distance between player 1 and 2
//        Mesh mesh = new Box(0.02f, 0.02f, 0.02f);
//        Geometry geo = new Geometry("testBox", mesh);
//        geo.setLocalTranslation(camNodeStartPos);
//        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        m.setColor("Color", ColorRGBA.Blue);
//        geo.setMaterial(m);
//        
//        camControlNode.attachChild(geo);
        
        // Camera Node
        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);     
        camNode.setLocalTranslation(0, 3.5f, 11f);
        camNode.lookAt(camControlNode.getLocalTranslation(), Vector3f.UNIT_Y);
        camControlNode.attachChild(camNode);
        rootNode.attachChild(camControlNode);
        
        chaseCamera = new ChaseCamera(cam, camControlNode, inputManager);
        chaseCamera.setSmoothMotion(true);
        chaseCamera.setDefaultDistance(50f);
        
    }
    
    private void exitGame()
    {
        StartScreen.start_game = false;
        
        if(exitGame)
        {
            resetGame();
            globalIngameMusic.stop();
            globalWindAudio.stop();
            guiNode.detachChild(guiSubNode);
            startScreen.setStartScreen();
            
        }
    }
    
    private void resetGame()
    {
        enemyWins = 0;
        ninjaWins = 0;
        gameRound = 1;
        newRound();
        gameFinish = false;
    }
    
    private void newRound()
    {
        String resetHP;
        time = 0;
        timeLeft = 60;
        timeRemain(time);
        
        ninjaAnimChannel.reset(true);
        ninjaAnimChannel.setAnim("Idle1", 0.5f);
        
        enemyAnimChannel.reset(true);
        enemyAnimChannel.setAnim("Idle1", 0.5f);
        
        gameFinishCountDown = 5f;
        roundFinishCountDown = 3f;
        enableKeys = false;
//        Switch = false;
        world.getNinjaNode().setUserData(strHealth, HEALTH);
        world.getEnemyNode().setUserData(strHealth, HEALTH);
        
        //-1 because hpBar image is 99% of pannel size
        resetHP = Float.toString(HEALTH-1);  
        startScreen.getEnemyHP(resetHP+"%");
        startScreen.getNinjaHP(resetHP+"%");
        
        world.getNinjaControl().warp(world.ninjaStartPos());
        world.getEnemyControl().warp(world.enemyStartPos());
        camControlNode.setLocalTranslation(camNodeStartPos);
    }
    
    private void initKeys() {
        inputManager.addMapping(NinjaMoveLeft, new KeyTrigger(KeyInput.KEY_NUMPAD4));
        inputManager.addMapping(NinjaMoveRight, new KeyTrigger(KeyInput.KEY_NUMPAD6));
        inputManager.addMapping(NinjaMoveUp, new KeyTrigger(KeyInput.KEY_NUMPAD8));
        inputManager.addMapping(NinjaMoveDown, new KeyTrigger(KeyInput.KEY_NUMPAD5));
        inputManager.addMapping(NinjaKick, new KeyTrigger(KeyInput.KEY_NUMPAD9));
        inputManager.addMapping(NinjaATK, new KeyTrigger(KeyInput.KEY_NUMPAD7));
        inputManager.addMapping(NinjaJump, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping(EnemyMoveLeft, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(EnemyMoveRight, new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(EnemyMoveUp, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(EnemyMoveDown, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(EnemyKick, new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping(EnemyATK, new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping(EnemyJump, new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping(ExitGame, new KeyTrigger(KeyInput.KEY_X));
        inputManager.addMapping(ResetGame, new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping(ChangeMusic, new KeyTrigger(KeyInput.KEY_C),
                new KeyTrigger(KeyInput.KEY_RCONTROL));
        
        inputManager.addListener(this, NinjaMoveLeft, NinjaMoveRight, NinjaMoveUp,
                NinjaMoveDown, NinjaJump, NinjaKick, NinjaATK, EnemyMoveLeft, 
                EnemyMoveRight, EnemyMoveUp, EnemyMoveDown, EnemyJump, 
                EnemyKick, EnemyATK, ExitGame, ResetGame, ChangeMusic);    } 
    
    private void timePassed(float tpf)
    {
        if (!gameFinish){ time +=tpf; }
    }
    
    private void timeRemain(float tpf)
    {
        if (!gameFinish){ timeLeft -=tpf; }
        String gameTime = Integer.toString((int)timeLeft);
        guiGameTime.setText("Time: " + gameTime);
    }
    
    public void onAction(String name, boolean isPressed, float tpf) 
    {
        if(StartScreen.start_game) {
            globalStartMusic.stop();
//            globalIngameMusic.play();
//            globalWindAudio.play();
            if(!enableKeys){ return; }
            
            if (world.getNinjaControl().isOnGround() && name.equals(NinjaMoveLeft))  {
                if (isPressed) { 
                    ninjaMoveLeft = true;
                    ninjaAnimChannel.setAnim(Walk, 0.2f);
                    ninjaAnimChannel.setLoopMode(LoopMode.Loop);
                    ninjaAnimChannel.setSpeed(1.5f);
                } else { 
                    ninjaMoveLeft = false;
                    ninjaAnimChannel.setAnim("Idle1", 0.05f);
                    ninjaAnimChannel.setLoopMode(LoopMode.Loop);
                }
            } 
            else if (world.getNinjaControl().isOnGround() && name.equals(NinjaMoveRight)) {
                if (isPressed) { 
                    ninjaMoveRight = true;
                    ninjaAnimChannel.setAnim(Backflip, 0.01f);
                    ninjaAnimChannel.setLoopMode(LoopMode.Loop);
                    ninjaAnimChannel.setSpeed(1.5f);
                    ninjaBackwardAudio.playInstance();
                } else { 
                    ninjaMoveRight = false; 
                    ninjaAnimChannel.setAnim("Idle1", 0.51f);
                    ninjaAnimChannel.setLoopMode(LoopMode.Loop);
                }
            } 
            else if (name.equals(NinjaMoveUp)) {
                if (isPressed) { ninjaMoveUp = true; } 
                else { ninjaMoveUp = false; }
            }
            else if (name.equals(NinjaMoveDown)) {
                if (isPressed) { ninjaMoveDown = true; } 
                else { ninjaMoveDown = false; }
            } 
            else if (name.equals(NinjaJump) && isPressed) {
                //Jump only when on ground!
                if (world.getNinjaControl().isOnGround()) {
                    ninjaAnimChannel.setAnim(Jump, 0.3f);
                    ninjaAnimChannel.setLoopMode(LoopMode.DontLoop);
                    world.getNinjaControl().jump();
                    ninjaAnimChannel.setSpeed(1.1f);
                    ninjaJumpAudio.playInstance();
                } else { 
                    ninjaAnimChannel.setAnim("Idle1", 2f);
                    ninjaAnimChannel.setLoopMode(LoopMode.Loop);
                }
            }
            else if (!ninjaAnimChannel.getAnimationName().equals(ATK) && 
                    name.equals(NinjaATK)) {
                if( isPressed){
                    ninjaAtk = true;
                    ninjaAnimChannel.setAnim(ATK, 0.2f);
                    ninjaAnimChannel.setLoopMode(LoopMode.DontLoop);
                    if((int)time%2 == 0) //Shout once ever 2 sec
                    {ninjaPunchAudio.playInstance();}
                } else { ninjaAtk = false;}
            }
            else if (name.equals(NinjaKick) && isPressed) {
                if (!ninjaAnimChannel.getAnimationName().equals(Kick)) {
                    ninjaKick = true;
                    ninjaAnimChannel.setAnim(Kick);
                    ninjaAnimChannel.setLoopMode(LoopMode.DontLoop);
                    ninjaAnimChannel.setSpeed(1.2f);
                    if((int)time%2 == 0) {ninjaKickAudio.playInstance();}
                }
                else if (!(world.getNinjaControl().isOnGround() && 
                        ninjaAnimChannel.getAnimationName().equals(Kick))){
                    ninjaKick = true;
                    ninjaAnimChannel.setAnim("SideKick", 0.4f);
                    ninjaAnimChannel.setLoopMode(LoopMode.DontLoop);
                    ninjaAnimChannel.setSpeed(1f);
                    if((int)time%2 == 0) {ninjaKickAudio.playInstance();}
                } else { ninjaKick = false; }
            }
            if (world.getEnemyControl().isOnGround() && name.equals(EnemyMoveLeft)) {
                if (isPressed)  { 
                    enemyMoveLeft = true;
                    enemyAnimChannel.setAnim(Backflip, 0.01f);
                    enemyAnimChannel.setLoopMode(LoopMode.Loop);
                    enemyAnimChannel.setSpeed(1.5f);
                    enemyBackwardAudio.playInstance();
                } else { 
                    enemyMoveLeft = false;
                    enemyAnimChannel.setAnim("Idle1", 0.05f);
                    enemyAnimChannel.setLoopMode(LoopMode.Loop);
                }
            }
            else if (world.getEnemyControl().isOnGround() && name.equals(EnemyMoveRight)){
                if (isPressed) { 
                    enemyMoveRight = true;
                    enemyAnimChannel.setAnim(Walk, 0.02f);
                    enemyAnimChannel.setLoopMode(LoopMode.Loop);
                    enemyAnimChannel.setSpeed(1.2f);
                } else { 
                    enemyMoveRight = false; 
                    enemyAnimChannel.setAnim("Idle1", 0.51f);
                    enemyAnimChannel.setLoopMode(LoopMode.Loop);
                }
            } 
            else if (name.equals(EnemyMoveUp)) {
                if (isPressed) { enemyMoveUp = true; } 
                else { enemyMoveUp = false; }
            }
            else if (name.equals(EnemyMoveDown)) {
                if (isPressed) { enemyMoveDown = true; } 
                else { enemyMoveDown = false; }
            } 
            else if (name.equals(EnemyJump) && isPressed) {
                //Jump only when on ground!
                if (world.getEnemyControl().isOnGround()){
                    enemyAnimChannel.setAnim(Jump, 0.3f);
                    enemyAnimChannel.setLoopMode(LoopMode.DontLoop);
                    world.getEnemyControl().jump();
                    enemyAnimChannel.setSpeed(1.1f);
                    enemyJumpAudio.playInstance();
                } else { 
                    enemyAnimChannel.setAnim("Idle1", 2f);
                    enemyAnimChannel.setLoopMode(LoopMode.Loop);
                }
            }
            else if (!enemyAnimChannel.getAnimationName().equals(ATK) && 
                    name.equals(EnemyATK))  {
                if( isPressed){
                    enemyAtk = true;
                    enemyAnimChannel.setAnim(ATK, 0.2f);
                    enemyAnimChannel.setLoopMode(LoopMode.DontLoop);
                    if((int)time%2 == 0) //Shout once ever 2 sec
                    {enemyPunchAudio.playInstance();}
                } else { enemyAtk = false;}
            }
            else if (name.equals(EnemyKick) && isPressed) {
                if (!enemyAnimChannel.getAnimationName().equals(Kick)){
                    enemyKick = true;
                    enemyAnimChannel.setAnim(Kick);
                    enemyAnimChannel.setLoopMode(LoopMode.DontLoop);
                    enemyAnimChannel.setSpeed(1.2f);
                    if((int)time%2 == 0) {enemyKickAudio.playInstance();}
                }
                else if (!(world.getNinjaControl().isOnGround() && 
                        enemyAnimChannel.getAnimationName().equals(Kick))){
                    enemyKick = true;
                    enemyAnimChannel.setAnim("SideKick", 0.4f);
                    enemyAnimChannel.setLoopMode(LoopMode.DontLoop);
                    enemyAnimChannel.setSpeed(1f);
                    if((int)time%2 == 0) {enemyKickAudio.playInstance();}
                } else { enemyKick = false; }
            }
            if (name.equals(ExitGame)) {
                if (isPressed) { 
                    exitGame = true;
                    exitGame(); 
                }
            } else if (name.equals(ResetGame)) {
                if (isPressed) { resetGame(); }
            }
            else if (name.equals(ChangeMusic) && isPressed) 
            {
                change = !change;
            }
        }
    }
    
    public void collision(PhysicsCollisionEvent event) 
    {
        if (getPhysicsSpace() == null) {
            return;
        }

        String nameA = event.getNodeA().getName();
        String nameB = event.getNodeB().getName();
        
        if (nameA.equals("Sword") && nameB.equals("enemyControlNode") || 
                nameA.equals("enemyControlNode") && nameB.equals("Sword")){
            Switch = true;
            float enemyHealth = (Float) world.getEnemyNode().getUserData(strHealth);
            float currentHP;
            if(ninjaAtk){
                enemyPunchHitAudio.playInstance();
                globalPunchAudio.playInstance();
                currentHP =  enemyHealth - ATK_DMG;
                world.getEnemyNode().setUserData(strHealth, currentHP);
                hp = Float.toString(currentHP); 
                startScreen.getEnemyHP(hp+"%");
                ninjaAtk = false;
                pEmitter.setLocalTranslation(event.getPositionWorldOnA());
                pEmitter.emitAllParticles();
            }
        }
            
        if (nameA.equals("LeftFoot") && nameB.equals("enemyControlNode") || 
                nameA.equals("enemyControlNode") && nameB.equals("LeftFoot")){
            Switch = true;
            float enemyHealth = (Float) world.getEnemyNode().getUserData(strHealth);
            float currentHP; 
            if(ninjaKick) {
                enemyKickHitAudio.playInstance();
                globalKickAudio.playInstance();
                currentHP =  enemyHealth - KICK_DMG;
                world.getEnemyNode().setUserData(strHealth, currentHP);
                hp = Float.toString(currentHP); 
                startScreen.getEnemyHP(hp+"%");
                ninjaKick = false;
                pEmitter.setLocalTranslation(event.getPositionWorldOnB());
                pEmitter.emitAllParticles();
            }
            else if(!world.getNinjaControl().isOnGround()){
                ninjaMoveLeft = false; //check only left due to switched keys
            }
        }
        
        if (nameA.equals("RightFoot") && nameB.equals("enemyControlNode") || 
                nameA.equals("enemyControlNode") && nameB.equals("RightFoot"))
        {
            Switch = true;
            float enemyHealth = (Float) world.getEnemyNode().getUserData(strHealth);
            float currentHP; 
            if(ninjaKick)
            {
                enemyKickHitAudio.playInstance();
                globalKickAudio.playInstance();
                currentHP =  enemyHealth - KICK_DMG;
                world.getEnemyNode().setUserData(strHealth, currentHP);
                hp = Float.toString(currentHP); 
                startScreen.getEnemyHP(hp+"%");
                ninjaKick = false;
                pEmitter.setLocalTranslation(event.getPositionWorldOnB());
                pEmitter.emitAllParticles();
            }
            else if(!world.getNinjaControl().isOnGround())
            {
                ninjaMoveLeft = false; //check only left due to switched keys
            }
        }       
        if (nameA.equals("EnemySword") && nameB.equals("ninjaControlNode") || 
                nameA.equals("ninjaControlNode") && nameB.equals("EnemySword")){
            Switch = true;
            float ninjaHealth = (Float) world.getNinjaNode().getUserData(strHealth);
            float currentHP;
            if(enemyAtk){
                ninjaPunchHitAudio.playInstance();
                globalPunchAudio.playInstance();
                currentHP =  ninjaHealth - ATK_DMG;
                world.getNinjaNode().setUserData(strHealth, currentHP);
                hp = Float.toString(currentHP); 
                startScreen.getNinjaHP(hp+"%");
                enemyAtk = false;
                pEmitter.setLocalTranslation(event.getPositionWorldOnA());
                pEmitter.emitAllParticles();
            }
        }
        if (nameA.equals("EnemyLeftFoot") && nameB.equals("ninjaControlNode") || 
                nameA.equals("ninjaControlNode") && nameB.equals("EnemyLeftFoot")){
            Switch = true;
            float ninjaHealth = (Float) world.getNinjaNode().getUserData(strHealth);
            float currentHP;
            if(enemyKick){
                ninjaKickHitAudio.playInstance();
                globalKickAudio.playInstance();
                currentHP =  ninjaHealth - KICK_DMG;
                world.getNinjaNode().setUserData(strHealth, currentHP);
                hp = Float.toString(currentHP); 
                startScreen.getNinjaHP(hp+"%");
                enemyKick = false;
                pEmitter.setLocalTranslation(event.getPositionWorldOnA());
                pEmitter.emitAllParticles();
            }
            else if(!world.getEnemyControl().isOnGround()){
                enemyMoveLeft = false; //check only left due to switched keys
            }
        }
        if (nameA.equals("EnemyRightFoot") && nameB.equals("ninjaControlNode") || 
                nameA.equals("ninjaControlNode") && nameB.equals("EnemyRightFoot")){
            Switch = true;
            float ninjaHealth = (Float) world.getNinjaNode().getUserData(strHealth);
            float currentHP;
            if(enemyKick){
                ninjaKickHitAudio.playInstance();
                globalKickAudio.playInstance();
                currentHP =  ninjaHealth - KICK_DMG;
                world.getNinjaNode().setUserData(strHealth, currentHP);
                hp = Float.toString(currentHP); 
                startScreen.getNinjaHP(hp+"%");
                enemyKick = false;
                pEmitter.setLocalTranslation(event.getPositionWorldOnA());
                pEmitter.emitAllParticles();
            }
            else if(!world.getEnemyControl().isOnGround()){
                enemyMoveLeft = false; //check only left due to switched keys
            }
        }
        /* Check to see if players crashed in to a hidden 
         * wall and prevent stagnating. also check which
         * side players are one  */
        if(nameA.equals("frontWall") && nameB.equals("ninjaControlNode") || 
                nameA.equals("ninjaControlNode") && nameB.equals("frontWall"))
        {
            if (!switchDone){ninjaMoveDown = false;}
            else {ninjaMoveUp = false;}
        }
        else if(nameA.equals("backWall") && nameB.equals("ninjaControlNode") || 
                nameA.equals("ninjaControlNode") && nameB.equals("backWall"))
        {
            if (!switchDone){ninjaMoveUp = false;}
            else {ninjaMoveDown = false;}
        }
        else if(nameA.equals("leftWall") && nameB.equals("ninjaControlNode") || 
                nameA.equals("ninjaControlNode") && nameB.equals("leftWall"))
        {
            if (!switchDone){ninjaMoveLeft = false;}
            else {ninjaMoveRight = false;}
        }
        else if(nameA.equals("rightWall") && nameB.equals("ninjaControlNode") || 
                nameA.equals("ninjaControlNode") && nameB.equals("rightWall"))
        {
            if (!switchDone){ninjaMoveRight = false;}
            else {ninjaMoveLeft = false;}
        }
        if(nameA.equals("frontWall") && nameB.equals("enemyControlNode") || 
                nameA.equals("enemyControlNode") && nameB.equals("frontWall"))
        {
            if (!switchDone){enemyMoveDown = false;}
            else {enemyMoveUp = false;}
            
        }
        else if(nameA.equals("backWall") && nameB.equals("enemyControlNode") || 
                nameA.equals("enemyControlNode") && nameB.equals("backWall"))
        {
            if (!switchDone){enemyMoveUp = false;}
            else {enemyMoveDown = false;}
        }
        else if(nameA.equals("leftWall") && nameB.equals("enemyControlNode") || 
                nameA.equals("enemyControlNode") && nameB.equals("leftWall"))
        {
            if (!switchDone){enemyMoveLeft = false;}
            else {enemyMoveRight = false;}
            
        }
        else if(nameA.equals("rightWall") && nameB.equals("enemyControlNode") || 
                nameA.equals("enemyControlNode") && nameB.equals("rightWall"))
        {
            if (!switchDone){enemyMoveRight = false;}
            else {enemyMoveLeft = false;}
        }
    }
    
    @Override
    public void simpleUpdate(float tpf)
    {
        if (StartScreen.start_game)
        {
            globalStartMusic.stop();
            if (!gameFinish) 
            {
                if(!change)
                {
                    globalIngameMusic2.stop();
                    globalIngameMusic.play();
                    globalWindAudio.play();
                }
                else
                {
                    globalIngameMusic.stop();
                    globalWindAudio.stop();
                    globalIngameMusic2.play();
                }
                
                if (time == 0)
                {
                    exitGame = false;
                    guiNode.attachChild(guiSubNode);
                }
                if(time <= 3 && gameRound == 1)
                {
                    camNode.setEnabled(false);
                    chaseCamera.setEnabled(true);
                    chaseCamera.setDefaultDistance(40f-(float)(Math.E)*time*3);
                    chaseCamera.setDefaultHorizontalRotation(time*1.5f);
                    camControlNode.addControl(chaseCamera);
                }
                else
                {
                    camNode.setEnabled(true);
                    chaseCamera.setEnabled(false);
                    camControlNode.removeControl(chaseCamera);
                    
                }
                float enemyHealth = (Float) world.getEnemyNode().getUserData(strHealth);
                float ninjaHealth = (Float) world.getNinjaNode().getUserData(strHealth);
                
                camNodePosX = (world.getEnemyCoordX() + world.getPlayerCoordX())/2;
                float posZ = Math.abs(world.getEnemyCoordX() - world.getPlayerCoordX())*2;

                if(world.getCharDistance() >= 3f)
                {
                    camControlNode.setLocalTranslation(camNodePosX, 3.5f, posZ); 
                }
                else if(world.getCharDistance() < 3f)
                {
                    camControlNode.setLocalTranslation(camNodePosX, 3.5f, 6f); 
                }
                
                ninjaUpDir = world.getNinjaNode().getWorldRotation().mult(Vector3f.UNIT_X);
                ninjaRightDir = world.getNinjaNode().getWorldRotation().mult(Vector3f.UNIT_Z); 
                enemyDownDir = world.getEnemyNode().getWorldRotation().mult(Vector3f.UNIT_X);
                enemyLeftDir = world.getEnemyNode().getWorldRotation().mult(Vector3f.UNIT_Z);
                ninjaWalkDirection.set(0, 0, 0); enemyWalkDirection.set(0, 0, 0);
                ninjaUpDir.y = 0; ninjaRightDir.y = 0;
                enemyDownDir.y = 0; enemyLeftDir.y = 0;
                
                if (ninjaMoveLeft) 
                {
                    ninjaWalkDirection.addLocal(ninjaRightDir.negate().mult(moveSpeed));
                } 
                if (ninjaMoveRight) 
                {
                    ninjaWalkDirection.addLocal(ninjaRightDir.mult(moveSpeed));
                }
                if (ninjaMoveUp) 
                {
                    ninjaWalkDirection.addLocal(ninjaUpDir.mult(moveSpeed));
                }
                if (ninjaMoveDown) 
                {
                    ninjaWalkDirection.addLocal(ninjaUpDir.negate().mult(moveSpeed));
                }
                if (enemyMoveLeft) 
                {
                    enemyWalkDirection.addLocal(enemyLeftDir.mult(moveSpeed));
                } 
                if (enemyMoveRight) 
                {
                    enemyWalkDirection.addLocal(enemyLeftDir.negate().mult(moveSpeed));
                }
                if (enemyMoveUp) 
                {
                    enemyWalkDirection.addLocal(enemyDownDir.negate().mult(moveSpeed));
                }
                if (enemyMoveDown) 
                {
                    enemyWalkDirection.addLocal(enemyDownDir.mult(moveSpeed));
                }

                world.getNinjaControl().setWalkDirection(ninjaWalkDirection);
                world.getEnemyControl().setWalkDirection(enemyWalkDirection);
                
                if ( world.getCharDistance() > 1.5 && Switch && 
                        world.getPlayerCoordX() < world.getEnemyCoordX())
                {
                    switchCounterRight =  0;
                    if (switchCounterLeft < 1)
                    {
                        ninjaAnimChannel.setAnim("Spin", 0.2f);
                        ninjaAnimChannel.setLoopMode(LoopMode.DontLoop);
                        ninjaAnimChannel.setSpeed(1.5f);
                        enemyAnimChannel.setAnim("Spin", 0.2f);
                        enemyAnimChannel.setLoopMode(LoopMode.DontLoop);
                        enemyAnimChannel.setSpeed(1.5f);
                        Quaternion nRotate = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
                        nRotate.multLocal(ninjaViewDirection);
                        Quaternion eRotate = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
                        eRotate.multLocal(enemyViewDirection);
                        switchNinjaKeys();
                        switchEnemyKeys();
                        Switch = false;
                        switchDone = true;
                        switchCounterLeft +=1;
                    }
                }

                else if(world.getPlayerCoordX() > world.getEnemyCoordX() && 
                        world.getCharDistance() > 1.5 && Switch==true)
                {
                    switchCounterLeft = 0;
                    if (switchCounterRight < 1)
                    {
                        ninjaAnimChannel.setAnim("Spin", 0.2f);
                        ninjaAnimChannel.setLoopMode(LoopMode.DontLoop);
                        ninjaAnimChannel.setSpeed(1.5f);
                        enemyAnimChannel.setAnim("Spin", 0.2f);
                        enemyAnimChannel.setLoopMode(LoopMode.DontLoop);
                        enemyAnimChannel.setSpeed(1.5f);
                        Quaternion nRotate = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
                        nRotate.multLocal(ninjaViewDirection);
                        Quaternion eRotate = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
                        eRotate.multLocal(enemyViewDirection);
                        switchNinjaKeys();
                        switchEnemyKeys();
                        Switch = false;
                        switchDone = false;
                        switchCounterRight +=1;
                    }
                }
                world.getNinjaControl().setViewDirection(ninjaViewDirection);
                world.getEnemyControl().setViewDirection(enemyViewDirection);
//                
                
                timePassed(tpf);
                updateGuiSound(tpf);
                checkGameState(enemyHealth, ninjaHealth, tpf);
            }
            else
            {
                exitGame = true;
                exitGame();
                globalStartMusic.play();
            }
        }
    }

    private void switchNinjaKeys()
    {
        String tmpUP, tmpSIDES ="";
        boolean tmpUp, tmpSides;
        
        tmpUP = NinjaMoveUp;
        NinjaMoveUp = NinjaMoveDown;
        NinjaMoveDown = tmpUP;

        tmpSIDES = NinjaMoveLeft;
        NinjaMoveLeft = NinjaMoveRight;
        NinjaMoveRight = tmpSIDES;
        
        tmpUp = ninjaMoveUp;
        ninjaMoveUp = ninjaMoveDown;
        ninjaMoveDown = tmpUp;

        tmpSides = ninjaMoveLeft;
        ninjaMoveLeft = ninjaMoveRight;
        ninjaMoveRight = tmpSides;
        
        //Set everything to false to avoid getting weird behavior
        ninjaMoveLeft = false;
        ninjaMoveRight = false;
        ninjaMoveUp = false;
        ninjaMoveDown = false;
    }
    
    private void switchEnemyKeys()
    {
        String tmpUP, tmpSIDES ="";
        boolean tmpUp, tmpSides;
        
        tmpUP = EnemyMoveUp;
        EnemyMoveUp = EnemyMoveDown;
        EnemyMoveDown = tmpUP;

        tmpSIDES = EnemyMoveLeft;
        EnemyMoveLeft = EnemyMoveRight;
        EnemyMoveRight = tmpSIDES;
        
        tmpUp = enemyMoveUp;
        enemyMoveUp = enemyMoveDown;
        enemyMoveDown = tmpUp;

        tmpSides = enemyMoveLeft;
        enemyMoveLeft = enemyMoveRight;
        enemyMoveRight = tmpSides;
        
        //Set everything to false to avoid getting weird behavior
        enemyMoveLeft = false;
        enemyMoveRight = false;
        enemyMoveUp = false;
        enemyMoveDown = false;
    }
    
    private PhysicsSpace getPhysicsSpace() 
    {
        return bulletAppState.getPhysicsSpace();
    }
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) 
    {
        if(ninjaAnimChannel.getAnimationName().equals(Kick))
        {
            ninjaAnimChannel.setAnim("Idle1", 0.3f);
            ninjaAnimChannel.setLoopMode(LoopMode.Loop);
            ninjaAnimChannel.setSpeed(1.2f);
            ninjaKick = false;  //else it wil do dmg on contact even if false
        }
        if(ninjaAnimChannel.getAnimationName().equals(ATK))
        {
            ninjaAnimChannel.setAnim("Idle1", 0.3f);
            ninjaAnimChannel.setLoopMode(LoopMode.Loop);
            ninjaAnimChannel.setSpeed(1.2f);
            ninjaAtk = false;
        }
        
        if(ninjaAnimChannel.getAnimationName().equals(Jump) && 
                (ninjaMoveLeft || ninjaMoveRight))
        {
            ninjaAnimChannel.setAnim("Idle1", 1.5f);
            ninjaAnimChannel.setLoopMode(LoopMode.Loop);
            ninjaAnimChannel.setSpeed(1.2f);
            if (ninjaMoveLeft){ninjaMoveLeft = false;}
            if (ninjaMoveRight){ninjaMoveRight = false;}
        }
        
        if(enemyAnimChannel.getAnimationName().equals(Kick))
        {
            enemyAnimChannel.setAnim("Idle1", 0.3f);
            enemyAnimChannel.setLoopMode(LoopMode.Loop);
            enemyAnimChannel.setSpeed(1.2f);
            enemyKick = false;  //else it wil do dmg on contact even if false
        }
        if(enemyAnimChannel.getAnimationName().equals(ATK))
        {
            enemyAnimChannel.setAnim("Idle1", 0.3f);
            enemyAnimChannel.setLoopMode(LoopMode.Loop);
            enemyAnimChannel.setSpeed(1.2f);
            enemyAtk = false;
        }
        
        if(enemyAnimChannel.getAnimationName().equals(Jump) && 
                (enemyMoveLeft || enemyMoveRight))
        {
            enemyAnimChannel.setAnim("Idle1", 1.5f);
            enemyAnimChannel.setLoopMode(LoopMode.Loop);
            enemyAnimChannel.setSpeed(1.2f);
            if (enemyMoveLeft){enemyMoveLeft = false;}
            if (enemyMoveRight){enemyMoveRight = false;}
        }
    }
    public void prePhysicsTick(PhysicsSpace space, float tpf) {}
    public void physicsTick(PhysicsSpace space, float tpf) {}
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {}
}
