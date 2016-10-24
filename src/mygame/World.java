/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.animation.SkeletonControl;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;

public class World
{
    private Material floorMat;
    private Box floor;
    private Vector3f lightDir = new Vector3f(-3.0f, -2.0f, -5.0f);
    private Vector3f gravity = new Vector3f(0f, 9.81f, 0f);
    private Vector3f ninjaStartPos = new Vector3f(2.5f, 6f, 12f);
    private Vector3f enemyStartPos = new Vector3f(-2.5f, 6f, 12f);
    private Vector3f jumpForce = new Vector3f(0f, 40f, 0f);
    private WaterFilter water;
    private Spatial terrain, ninja, enemy;
    private Geometry[] wallGeo;
    private Node worldNode = new Node("worldNode"); // for app state
    private Node ninjaNode = new Node("ninjaControlNode");
    private Node enemyNode = new Node("enemyControlNode");
    private BetterCharacterControl ninjaControl;
    private BetterCharacterControl enemyControl;
    private RigidBodyControl floorPhy;
    private RigidBodyControl wallPhy[];
    private Node hand = new Node("SwordGrip");
    private AssetManager assetManager;
    private final float RADIUS = 0.3f;
    private final float HEIGHT = 1.8f;
    private final float MASS = 10f;
    private int[] coordXYZ = new int[3];
    private ParticleEmitter dmgEffect;
    private ParticleEmitter fireSword;
    private ParticleEmitter fireSword2;
    
        
    public Node createTerrain(Application app) 
    {
        AssetManager am = app.getAssetManager();
        terrain = am.loadModel("Scenes/TerrainTest.j3o");        
        terrain.setLocalTranslation(0, -7, 0);
        terrain.setShadowMode(RenderQueue.ShadowMode.Receive);
        
        worldNode.attachChild(terrain);
        return worldNode;
    }
    
    public Node createNinja(BulletAppState bulletAppState)
    {
        Node s = new Node("SwordCollisionNode");
        Node rf = new Node("RightFootCollisionNode");
        Node lf = new Node("LeftFootCollisionNode");
        
                
        RigidBodyControl playerBoxControl[] = new RigidBodyControl[3];
        Box b = new Box(5f,5f,5f);
        Geometry g[] = new Geometry[3];
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1, 0, 0, 0));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
       
        g[0] = new Geometry("Sword", b);
        g[1] = new Geometry("LeftFoot", b);
        g[2] = new Geometry("RightFoot", b);
       
        g[0].setMaterial(mat);
        g[0].setQueueBucket(RenderQueue.Bucket.Translucent);
        g[1].setMaterial(mat);
        g[1].setQueueBucket(RenderQueue.Bucket.Translucent);
        g[2].setMaterial(mat);
        g[2].setQueueBucket(RenderQueue.Bucket.Translucent);
        
        //Some offset due to players' collision capsule size
        g[0].move(0, 0, 30);
        g[1].move(0, 10, 0);
        g[2].move(0, 10, 0);
       
        //Control Node
        ninjaNode.setLocalScale(0.0105f, 0.0105f, 0.0105f);
        ninjaNode.setLocalTranslation(ninjaStartPos);
        
        //Create Player 
        ninja = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
        ninja.setName("ninja");
        ninja.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        
        SkeletonControl skeletonControl = ninja.getControl(SkeletonControl.class);
        
        rf = skeletonControl.getAttachmentsNode("Joint20");
        s = skeletonControl.getAttachmentsNode("Joint29");
        lf = skeletonControl.getAttachmentsNode("Joint25");
        hand = skeletonControl.getAttachmentsNode("Joint13");
        
        s.attachChild(g[0]);
        lf.attachChild(g[1]);
        rf.attachChild(g[2]);
        
        // Create Control
        ninjaControl = new BetterCharacterControl(RADIUS, HEIGHT, MASS);
        ninjaControl.setViewDirection(new Vector3f(1,0,0));
        ninjaControl.setJumpForce(jumpForce);
        ninjaControl.setGravity(gravity);
                
        CollisionShape boxCollisionShape = new BoxCollisionShape(new Vector3f(0.05f,0.05f,0.05f));
        
        //Sword box
        playerBoxControl[0] = new RigidBodyControl(boxCollisionShape, 1f);
        g[0].addControl(playerBoxControl[0]);
        playerBoxControl[0].setKinematic(true);
        bulletAppState.getPhysicsSpace().add(playerBoxControl[0]);
        
        //Left foot Box
        playerBoxControl[1] = new RigidBodyControl(boxCollisionShape, 1f);
        g[1].addControl(playerBoxControl[1]);
        playerBoxControl[1].setKinematic(true);
        bulletAppState.getPhysicsSpace().add(playerBoxControl[1]);
        
        //Right foot Box
        playerBoxControl[2] = new RigidBodyControl(boxCollisionShape, 1f);
        g[2].addControl(playerBoxControl[2]);
        playerBoxControl[2].setKinematic(true);
        bulletAppState.getPhysicsSpace().add(playerBoxControl[2]);
        
        
        fireSword = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 32);
        fireSword2 = new ParticleEmitter("Emitter2", ParticleMesh.Type.Triangle, 32);
        
        Material matS = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        matS.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));

        fireSword.setMaterial(matS);
        fireSword.setImagesX(2);
        fireSword.setImagesY(2); // 2x2 texture animation
        fireSword.setEndColor(new ColorRGBA(0.9f, 0.9f, 0.1f, 0.5f)); 
        fireSword.setStartColor(new ColorRGBA(1f, 0f, 0.1f, 1f));
        fireSword.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, -3));
        fireSword.getParticleInfluencer().setVelocityVariation(0.1f);
        fireSword.setGravity(0, 0, 0);
        fireSword.setStartSize(0.2f);
        fireSword.setEndSize(0.15f);
        fireSword.setLowLife(0.15f);
        fireSword.setHighLife(0.15f);
        fireSword.setNumParticles(1);
        fireSword.setQueueBucket(RenderQueue.Bucket.Opaque);
        hand.attachChild(fireSword);
        
        fireSword2.setMaterial(matS);
        fireSword2.setImagesX(2);
        fireSword2.setImagesY(2); // 2x2 texture animation
        fireSword2.setEndColor(new ColorRGBA(0.9f, 0.9f, 0.1f, 0.6f)); 
        fireSword2.setStartColor(new ColorRGBA(1f, 0.0f, 0.0f, 0.9f));
        fireSword2.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 3f));
        fireSword2.getParticleInfluencer().setVelocityVariation(0.1f);
        fireSword2.setGravity(0, 0, 0);
        fireSword2.setStartSize(0.2f);
        fireSword2.setEndSize(0.15f);
        fireSword2.setLowLife(0.15f);
        fireSword2.setHighLife(0.15f);
        fireSword2.setRandomAngle(true);
        fireSword.setNumParticles(20);
        fireSword2.setQueueBucket(RenderQueue.Bucket.Opaque);
        s.attachChild(fireSword2);
        
        bulletAppState.getPhysicsSpace().add(ninjaControl);
        ninjaNode.attachChild(ninja);
        ninjaNode.addControl(ninjaControl);
        worldNode.attachChild(ninjaNode);
        
        return worldNode;
    }
    
    public Node createEnemy(BulletAppState bulletAppState)
    {
        Node s = new Node("EnemySwordCollisionNode");
        Node rf = new Node("EnemyRightFootCollisionNode");
        Node lf = new Node("EnemyLeftFootCollisionNode");
        
        RigidBodyControl playerBoxControl[] = new RigidBodyControl[3];
        Box b = new Box(5f,5f,5f);
        Geometry eg[] = new Geometry[3];
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1, 0, 0, 0));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
       
        eg[0] = new Geometry("EnemySword", b);
        eg[1] = new Geometry("EnemyLeftFoot", b);
        eg[2] = new Geometry("EnemyRightFoot", b);
       
        eg[0].setMaterial(mat);
        eg[0].setQueueBucket(RenderQueue.Bucket.Translucent);
        eg[1].setMaterial(mat);
        eg[1].setQueueBucket(RenderQueue.Bucket.Translucent);
        eg[2].setMaterial(mat);
        eg[2].setQueueBucket(RenderQueue.Bucket.Translucent);
        
        //Some offset due to players' collision capsule size
        eg[0].move(0, 0, 30);
        eg[1].move(0, 10, 0);
        eg[2].move(0, 10, 0);
        
        //Control Node
        enemyNode.setLocalScale(0.0105f, 0.0105f, 0.0105f);
        enemyNode.setLocalTranslation(enemyStartPos);  
        
        //Create Enemy 
        enemy = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
        enemy.setName("enemy");
        enemy.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        
        SkeletonControl skeletonControl = enemy.getControl(SkeletonControl.class);
        
        rf = skeletonControl.getAttachmentsNode("Joint20");
        s = skeletonControl.getAttachmentsNode("Joint29");
        lf = skeletonControl.getAttachmentsNode("Joint25");
        s.attachChild(eg[0]);
        lf.attachChild(eg[1]);
        rf.attachChild(eg[2]);
        
        // Create Control
        enemyControl = new BetterCharacterControl(RADIUS, HEIGHT, MASS);
        enemyControl.setViewDirection(new Vector3f(-1,0,0));
        enemyControl.setJumpForce(jumpForce);
        enemyControl.setGravity(gravity);
        
        CollisionShape boxCollisionShape = new BoxCollisionShape(new Vector3f(0.05f,0.05f,0.05f));
        
        //Sword box
        playerBoxControl[0] = new RigidBodyControl(boxCollisionShape, 1f);
        eg[0].addControl(playerBoxControl[0]);
        playerBoxControl[0].setKinematic(true);
        bulletAppState.getPhysicsSpace().add(playerBoxControl[0]);
        
        //Left foot Box
        playerBoxControl[1] = new RigidBodyControl(boxCollisionShape, 1f);
        eg[1].addControl(playerBoxControl[1]);
        playerBoxControl[1].setKinematic(true);
        bulletAppState.getPhysicsSpace().add(playerBoxControl[1]);
        
        //Right foot Box
        playerBoxControl[2] = new RigidBodyControl(boxCollisionShape, 1f);
        eg[2].addControl(playerBoxControl[2]);
        playerBoxControl[2].setKinematic(true);
        bulletAppState.getPhysicsSpace().add(playerBoxControl[2]);
        
        //Get physics and attach
        bulletAppState.getPhysicsSpace().add(enemyControl);
        enemyNode.attachChild(enemy);
        enemyNode.addControl(enemyControl);
        worldNode.attachChild(enemyNode);
        
        return worldNode;
    }
    
    public Node createFloor(BulletAppState bulletAppState)
    {
        wallGeo = new Geometry[5];
        wallPhy = new RigidBodyControl[4];
        
        //Create box
        floor = new Box(17f, 0.1f, 1.5f);
        Box sideWalls = new Box(0.1f, 1.5f, 1.5f);
        
        //Create material
        floorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMat.setColor("Color", new ColorRGBA(1, 0, 0, 0));
        floorMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        //floorMat.getAdditionalRenderState().setWireframe(true);
        
        //Create floor
        wallGeo[0] = new Geometry("Floor", floor);
        wallGeo[0].setLocalTranslation(0f, 5.35f, 12f);
       
        wallGeo[1] = new Geometry("frontWall", floor);
        wallGeo[1].setLocalTranslation(0f, 6f, 12.66f);
        wallGeo[1].setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI/2, Vector3f.UNIT_X));
        
        wallGeo[2] = new Geometry("backWall", floor);
        wallGeo[2].setLocalTranslation(0f, 6f, 11.29f);
        wallGeo[2].setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI/2, Vector3f.UNIT_X));
        
        wallGeo[3] = new Geometry("rightWall", sideWalls);
        wallGeo[3].setLocalTranslation(17f, 6f, 12f);
        
        wallGeo[4] = new Geometry("leftWall", sideWalls);
        wallGeo[4].setLocalTranslation(-17f, 6f, 12f);
        
        /* Make the floor physical with mass 0.0f! */
        floorPhy = new RigidBodyControl(0f);
        
        for (int i = 0; i < wallGeo.length; i++)
        {
            wallGeo[i].setMaterial(floorMat);
            wallGeo[i].setQueueBucket(RenderQueue.Bucket.Translucent);
            
            if(i < wallPhy.length)
            {
                wallPhy[i] = new RigidBodyControl(0f);
            }  
            if(i == 0)
            {
                wallGeo[i].setShadowMode(RenderQueue.ShadowMode.Receive);
                wallGeo[i].addControl(floorPhy);
                bulletAppState.getPhysicsSpace().add(floorPhy);
                worldNode.attachChild(wallGeo[i]);
            }
            else
            {
                wallGeo[i].setShadowMode(RenderQueue.ShadowMode.Off);
                wallGeo[i].addControl(wallPhy[i-1]);
                bulletAppState.getPhysicsSpace().add(wallPhy[i-1]);
                worldNode.attachChild(wallGeo[i]);
            }
        }
        return worldNode;
    }
    
    public WaterFilter initPPcWater(Node world, Application app)
    { 
        assetManager = app.getAssetManager();
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        ViewPort viewPort = app.getViewPort();
        water = new WaterFilter(world, lightDir);
         
        BloomFilter bloom = new BloomFilter();
        bloom.setExposurePower(100);
        bloom.setBloomIntensity(2.0f);
        fpp.addFilter(bloom);
        
        LightScatteringFilter lsf = new LightScatteringFilter(new Vector3f(-4f,-2f,5).mult(-300));
        lsf.setLightDensity(1f);
        fpp.addFilter(lsf);
        
        DepthOfFieldFilter dof = new DepthOfFieldFilter();
        dof.setFocusDistance(0);
        dof.setFocusRange(60);
        fpp.addFilter(dof);
 
        fpp.addFilter(water);
        water.setCenter(Vector3f.ZERO);
        water.setRadius(2600); 
        water.setWaveScale(0.0015f); 
        water.setMaxAmplitude(4f); 
        water.setFoamExistence(new Vector3f(1f, 4f, 10.5f)); 
        water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg")); 
        water.setRefractionStrength(-0.2f); 
        water.setWaterHeight(2f); 
        viewPort.addProcessor(fpp);
        
        return water;
    }  
    
    public ParticleEmitter prepareEffect() 
    {
        dmgEffect = new ParticleEmitter("dmgEffect", ParticleMesh.Type.Triangle, 32);
        Material effectMat = new Material(assetManager, "MatDefs/Particle.j3md");
        effectMat.setTexture("Texture", assetManager.loadTexture("Textures/Effects/flame.png"));
        dmgEffect.setMaterial(effectMat);
        dmgEffect.setImagesX(2);
        dmgEffect.setImagesY(2);
        dmgEffect.setSelectRandomImage(true);
        dmgEffect.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.1f));
        dmgEffect.setEndColor(new ColorRGBA(0.1f, 0.1f, 0.8f, 0.4f));
        dmgEffect.setStartSize(0.1f);
        dmgEffect.setEndSize(0.5f);
        dmgEffect.setShape(new EmitterSphereShape(Vector3f.ZERO, 0.5f));
        dmgEffect.setParticlesPerSec(0);
        dmgEffect.setGravity(0, 0, -5); //z plane
        dmgEffect.setLowLife(.1f);
        dmgEffect.setHighLife(.2f);
        dmgEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 1));
        dmgEffect.getParticleInfluencer().setVelocityVariation(0.2f);
        dmgEffect.setQueueBucket(RenderQueue.Bucket.Opaque);
        return dmgEffect;
    }
    
    public Node getNinjaHand()
    {
        return hand;
    }
    public Node getNinjaNode()
    {
        return ninjaNode;
    }
    
    public Spatial getNinja()
    {
        return ninja;
    }
    
    public Vector3f ninjaStartPos()
    {
        return ninjaStartPos;
    }
    
    public Node getEnemyNode()
    {
        return enemyNode;
    }
    
    public Spatial getEnemy()
    {
        return enemy;
    }
    
    public ParticleEmitter getFireSword()
    {
        return fireSword;
    }
    public Vector3f enemyStartPos()
    {
        return enemyStartPos;
    }
    
    public BetterCharacterControl getNinjaControl()
    {
        return ninjaControl;
    }
    
    public BetterCharacterControl getEnemyControl()
    {
        return enemyControl;
    }
    
    public float getPlayerCoordX()
    {
        return ninjaNode.getLocalTranslation().getX();
    }
    
    public float getPlayerCoordY()
    {
        return ninjaNode.getLocalTranslation().getY();
    }
    
    public float getPlayerCoordZ()
    {
        return ninjaNode.getLocalTranslation().getZ();
    }
    
    public float getEnemyCoordX()
    {
        return enemyNode.getLocalTranslation().getX();
    }

    public float getCharDistance()
    {
        float playerCoordX = getPlayerCoordX();
        float enemyCoordX = getEnemyCoordX();      
        float dist = Math.abs(enemyCoordX-playerCoordX);
        return dist;
    }
    
    public int[] getPlayerCoordinates()
    {
        float coordX = getPlayerCoordX();
        float coordY = getPlayerCoordY();
        float coordZ = getPlayerCoordZ();
        
        coordXYZ[0] = (int)coordX;
        coordXYZ[1] = (int)coordY;
        coordXYZ[2] = (int)coordZ;
        
        return coordXYZ;
    }
}
