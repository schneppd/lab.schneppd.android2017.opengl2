package com.schneppd.myopenglapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.imageBitmap
import java.io.File
import android.os.Environment.DIRECTORY_PICTURES
import android.support.v4.content.FileProvider
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.*
import java.text.SimpleDateFormat
import java.util.*
import android.text.method.Touch.onTouchEvent
import android.view.MotionEvent
import android.graphics.PointF
import android.R.attr.y
import android.R.attr.x
import android.R.attr.mode
import android.graphics.Matrix
import android.util.FloatMath
import com.almeros.android.multitouch.MoveGestureDetector
import com.almeros.android.multitouch.RotateGestureDetector


/**
 * Created by david.schnepp on 13/07/2017.
 */
class MainActivityTest2 : AppCompatActivity(), View.OnTouchListener
{

    companion object Static {
        val REQUEST_IMAGE_CAPTURE = 1
        val REQUEST_TAKE_PHOTO = 1
        val DEBUG_TAG = "Main"
    }

    var isScaleMotionDetected = false
    var currentPhotoPath = ""

    private var mScaleFactor = 1.0f
    private var mRotationDegrees = 0f
    private var mFocusX = 0f
    private var mFocusY = 0f

    lateinit var mScaleDetector:ScaleGestureDetector
    lateinit var mRotateDetector:RotateGestureDetector
    lateinit var mMoveDetector:MoveGestureDetector



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        //svUserModel.setOnTouchListener(this)
        mScaleDetector = ScaleGestureDetector(applicationContext, ScaleListener())
        mRotateDetector = RotateGestureDetector(applicationContext, RotateListener())
        mMoveDetector = MoveGestureDetector(applicationContext, MoveListener())

        /*
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view -> onClickTestButton(view) }
        */
    }

    override fun onDestroy() {
        if(currentPhotoPath.isEmpty()){
            deletePreviousFile()
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when(item.itemId){
            R.id.action_photo -> onTakePhoto()
            R.id.action_import_model -> onChangeModel()
        }

        return super.onOptionsItemSelected(item)
    }



    fun onChangeModel() {
        //Snackbar.make(v, "Change model", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        if(svUserModel.visibility == View.VISIBLE)
            svUserModel.visibility = View.INVISIBLE
        else
            svUserModel.visibility = View.VISIBLE
        svUserModel.loadModel()
    }

    fun onTakePhoto() {
        //Snackbar.make(v, "Taking photo for background", Snackbar.LENGTH_LONG).setAction("Action", null).show()


        val takePictureIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val serviceProvider = takePictureIntent.resolveActivity(packageManager)
        serviceProvider?.let {
            if(currentPhotoPath.isEmpty()){
                deletePreviousFile()
            }

            var photoFile: File? = createImageSaveFile()
            photoFile?: return
            val photoURI = FileProvider.getUriForFile(this, "com.schneppd.myopenglapp.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }?: Snackbar.make(ivUserPicture, "No photo app installed", Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    fun deletePreviousFile(){
        val fileUri = "file://" + currentPhotoPath
        val file = File(fileUri)
        file.delete()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val fileUri = "file://" + currentPhotoPath
            Picasso.with(this).load(fileUri).resize(ivUserPicture.width, ivUserPicture.height).into(ivUserPicture)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createImageSaveFile() : File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
        currentPhotoPath = image.absolutePath

        return image

    }
    override fun onTouch(v:View, event:MotionEvent):Boolean {
        mScaleDetector.onTouchEvent(event)
        mRotateDetector.onTouchEvent(event)
        mMoveDetector.onTouchEvent(event)

        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor // scale change since previous event
            return true
        }
    }

    private inner class RotateListener : RotateGestureDetector.SimpleOnRotateGestureListener() {
        override fun onRotate(detector: RotateGestureDetector?): Boolean {
            mRotationDegrees -= detector!!.rotationDegreesDelta
            return true
        }
    }

    private inner class MoveListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        override fun onMove(detector: MoveGestureDetector?): Boolean {
            val d = detector!!.focusDelta
            mFocusX += d.x
            mFocusY += d.y

            return true
        }
    }


}