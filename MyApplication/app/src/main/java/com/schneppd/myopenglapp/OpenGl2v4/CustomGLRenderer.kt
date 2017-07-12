package com.schneppd.myopenglapp.OpenGl2v4

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import com.schneppd.myopenglapp.R
import org.rajawali3d.Object3D
import org.rajawali3d.lights.DirectionalLight
import org.rajawali3d.loader.ALoader
import org.rajawali3d.loader.LoaderOBJ
import org.rajawali3d.loader.async.IAsyncLoaderCallback
import org.rajawali3d.materials.Material
import org.rajawali3d.materials.methods.DiffuseMethod
import org.rajawali3d.materials.textures.ATexture
import org.rajawali3d.materials.textures.Texture
import org.rajawali3d.math.vector.Vector3
import org.rajawali3d.primitives.Sphere
import org.rajawali3d.renderer.Renderer
import org.rajawali3d.util.RajLog

/**
 * Created by david.schnepp on 12/07/2017.
 */
class CustomGLRenderer(context:Context) : Renderer(context), IAsyncLoaderCallback {
    lateinit private var directionalLight: DirectionalLight
    lateinit private var earthSphere: Sphere
    private var gate: Object3D? = null


    init{
        setFrameRate(60)

    }

    override fun initScene(){
        directionalLight = DirectionalLight(1.0, .2, -1.0)
        directionalLight.setColor(1.0f, 1.0f, 1.0f)
        directionalLight.power = 2.0f
        currentScene.addLight(directionalLight)

        val material = Material()
        material.enableLighting(true)
        material.diffuseMethod = DiffuseMethod.Lambert()
        material.color = 0

        val earthTexture = Texture("Earth", R.drawable.earth)
        try{
            material.addTexture(earthTexture)
        } catch(error:ATexture.TextureException){
            Log.d("DEBUG", "TEXTURE ERROR")
        }

        earthSphere = Sphere(1f, 24, 24)
        earthSphere.material = material
        //currentScene.addChild(earthSphere)

        val loaderOBJ = LoaderOBJ(context.resources, mTextureManager, R.raw.gate2_test_mobile)
        loadModel(loaderOBJ, this, R.raw.gate2_test_mobile)

        currentCamera.z = 4.2
    }

    override fun onRender(ellapsedRealtime:Long, deltaTime:Double){
        super.onRender(ellapsedRealtime, deltaTime)
        //earthSphere.rotate(Vector3.Axis.Y, 1.0)
        gate?.let{
            gate!!.rotate(Vector3.Axis.Y, 1.0)
        }
    }

    override fun onTouchEvent(event:MotionEvent){

    }

    override fun onOffsetsChanged(x:Float, y:Float, z:Float, w:Float, i:Int, j:Int){

    }

    override fun onModelLoadComplete(aLoader:ALoader){
        val obj = aLoader as LoaderOBJ
        gate = obj.parsedObject
        gate!!.position = Vector3.ZERO
        currentScene.addChild(gate)
    }

    override fun onModelLoadFailed(aLoader:ALoader){
        val b = 1 //error
    }
}