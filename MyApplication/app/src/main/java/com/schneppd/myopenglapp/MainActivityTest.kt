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


/**
 * Created by david.schnepp on 13/07/2017.
 */
class MainActivityTest : AppCompatActivity(), View.OnTouchListener
{

    companion object Static {
        val REQUEST_IMAGE_CAPTURE = 1
        val REQUEST_TAKE_PHOTO = 1
        val DEBUG_TAG = "Main"
    }

    var isScaleMotionDetected = false
    var currentPhotoPath = ""

    var matrix = Matrix()
    var savedMatrix  = Matrix()
    // we can be in one of these 3 states
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE
    // remember some things for zooming
    private var start = PointF()
    private val mid = PointF()
    private var oldDist = 1.0
    private var d = 0.0
    private var newRot = 0.0
    private var lastEvent: FloatArray? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        svUserModel.setOnTouchListener(this)

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

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // handle touch events here
        val view = svUserModel
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
                lastEvent = null
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10.0) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
                lastEvent = FloatArray(4)
                lastEvent!![0] = event.getX(0)
                lastEvent!![1] = event.getX(1)
                lastEvent!![2] = event.getY(0)
                lastEvent!![3] = event.getY(1)
                d = rotation(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                matrix.set(savedMatrix)
                val dx = event.x - start.x
                val dy = event.y - start.y
                matrix.postTranslate(dx, dy)
                Log.d(DEBUG_TAG, "you drag ${dx}  ${dy}")
            } else if (mode == ZOOM) {
                val newDist = spacing(event)
                if (newDist > 10f && event.pointerCount <=2) {
                    matrix.set(savedMatrix)
                    val scale = newDist / oldDist
                    matrix.postScale(scale.toFloat(), scale.toFloat(), mid.x, mid.y)
                    Log.d(DEBUG_TAG, "you zoom ${scale}")
                }
                if (lastEvent != null && event.pointerCount == 3) {
                    newRot = rotation(event)
                    val r = newRot - d
                    val values = FloatArray(9)
                    matrix.getValues(values)
                    val tx = values[2]
                    val ty = values[5]
                    val sx = values[0]
                    val xc = view.getWidth() / 2 * sx
                    val yc = view.getHeight() / 2 * sx
                    matrix.postRotate(r.toFloat(), tx + xc, ty + yc)
                    Log.d(DEBUG_TAG, "you rotate  ${r}")
                }
            }
        }

        //view.setImageMatrix(matrix)
        return true
    }

    /**
     * Determine the space between the first two fingers
     */
    private fun spacing(event: MotionEvent): Double {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        val calc = x * x + y * y
        return Math.sqrt(calc.toDouble())
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    /**
     * Calculate the degree to be rotated by.

     * @param event
     * *
     * @return Degrees
     */
    private fun rotation(event: MotionEvent): Double {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(delta_y, delta_x)
        return Math.toDegrees(radians)
    }







}