package bytejam.project.turbo;

import bytejam.project.renderer.Renderer;

public abstract class Scene {

    protected Renderer renderer;
    protected Camera camera;

    //Scenes contain our objects, renderer, physics.
    //This class is kind of like a blue print of all of the Scenes we will have.
    public Scene() {
        
    }

    public abstract void init();

    public abstract void update(float dt);

    public Camera camera() {
        return this.camera;
    }

}
