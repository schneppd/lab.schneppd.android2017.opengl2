package com.schneppd.myopenglapp.OpenGl2v4

import android.content.Context
import android.graphics.Color
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
import org.rajawali3d.materials.methods.SpecularMethod
import org.rajawali3d.materials.textures.ATexture
import org.rajawali3d.materials.textures.Texture
import org.rajawali3d.math.vector.Vector3
import org.rajawali3d.primitives.Sphere
import org.rajawali3d.renderer.Renderer


/**
 * Created by david.schnepp on 12/07/2017.
 */
class CustomGLRenderer(context:Context) : Renderer(context), IAsyncLoaderCallback {
    lateinit private var directionalLight: DirectionalLight
    lateinit private var earthSphere: Sphere
    private var gate: Object3D? = null
    private var loaderOBJ:LoaderOBJ? = null


    var userScale:Float = 1.0f
        get() = field
        set(value) {field = value}
    fun getGateScale() : Double = 0.03 * userScale.toDouble()

    init{
        setFrameRate(60)

    }



    override fun initScene(){
        directionalLight = DirectionalLight(1.0, .2, -1.0)
        directionalLight.setColor(1.0f, 1.0f, 1.0f)
        directionalLight.power = 2.0f
        currentScene.addLight(directionalLight)

        earthSphere = Sphere(1f, 24, 24)
        earthSphere.material = getTextureAsMaterial("Earth", R.drawable.earth)
        //currentScene.addChild(earthSphere)



        currentCamera.z = 4.2
    }

    private fun getTextureAsMaterial(textureName:String, idResource:Int) : Material {
        val material = Material()
        material.enableLighting(true)
        material.diffuseMethod = DiffuseMethod.Lambert()
        material.color = 0

        val earthTexture = Texture(textureName, idResource)
        try{
            material.addTexture(earthTexture)
        } catch(error:ATexture.TextureException){
            Log.d("DEBUG", "TEXTURE ERROR")
        }
        return material
    }

    private fun getBasicMaterial() : Material {
        val material = Material()
        material.enableLighting(true)
        material.diffuseMethod = DiffuseMethod.Lambert()
        material.color = Color.GRAY //Color.argb(255, 192, 192, 192) //0xff009900 //c0c0c0
        material.colorInfluence = 0.7f
        material.specularMethod = SpecularMethod.Phong()

        return material
    }

    override fun onRender(ellapsedRealtime:Long, deltaTime:Double){
        super.onRender(ellapsedRealtime, deltaTime)
        //earthSphere.rotate(Vector3.Axis.Y, 1.0)
        gate?.setScale(getGateScale())
        //gate?.rotate(Vector3.Axis.Y, getGateScale())
    }

    override fun onTouchEvent(event:MotionEvent){
        val b = 1
    }

    override fun onOffsetsChanged(x:Float, y:Float, z:Float, w:Float, i:Int, j:Int){

    }

    override fun onModelLoadComplete(aLoader:ALoader){
        val obj = aLoader as LoaderOBJ
        gate = obj.parsedObject
        gate?.let{
            gate!!.position = Vector3.ZERO
            //gate!!.setAlpha(255)
            //gate!!.setColor(Vector3(0.753, 0.753, 0.753))
            gate!!.material = getBasicMaterial()
            //gate!!.isTransparent = false
            //gate!!.isBackSided = true
            gate!!.setScale(getGateScale())
            gate!!.rotate(Vector3.Axis.Y, 180.0)
            currentScene.addChild(gate)
        }
        loaderOBJ = null // clean up
        //serializeO


    }

    override fun onModelLoadFailed(aLoader:ALoader){
        val b = 1 //error
    }

    fun loadModel(){
        loaderOBJ = LoaderOBJ(context.resources, mTextureManager, R.raw.gate2_test_mobile)
        loadModel(loaderOBJ, this, R.raw.gate2_test_mobile)
    }

}