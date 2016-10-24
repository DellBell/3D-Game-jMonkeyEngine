/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;

/**
 * @author Delshad
 */
public class StartScreen extends AbstractAppState implements ScreenController 
{
    public static boolean start_game = false;
    private Nifty nifty;
    private Application app;
    private Screen screen;
    
    
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);

        this.app = (SimpleApplication) app;
//        this.stateManager = stateManager;
//        getEnemyHP();

    }
    
    public void startGame(String nextScreen) 
    {
        nifty.gotoScreen(nextScreen); // Switch to game screen
        start_game = true;
        
    }
    public void quitGame() 
    {
//        startGameNow = false;
        app.stop();
    }
    
    public void bind(Nifty nifty, Screen screen)
    {
        this.nifty = nifty;
        this.screen = screen;
    }
    
    public void initialize(Application app) 
    {
        this.app = app;
    }
    
    public void setStartScreen()
    {
        nifty.gotoScreen("start");
    }
    public void getEnemyHP(String hpValue)
    {
        Screen start = nifty.getScreen("hud");
        Element hp = start.findElementByName("enemyHP");
        hp.setConstraintWidth(new SizeValue(hpValue));
        hp.getParent().layoutElements();
    }
    
    public void getNinjaHP(String hpValue)
    {
        Screen start = nifty.getScreen("hud");
        Element hp = start.findElementByName("ninjaHP");
        hp.setConstraintWidth(new SizeValue(hpValue));
        hp.getParent().layoutElements();
    }
    
    public void onStartScreen() {}
    public void onEndScreen() {}
}
