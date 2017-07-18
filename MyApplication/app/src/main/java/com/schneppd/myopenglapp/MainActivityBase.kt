package com.schneppd.myopenglapp

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.ScaleGestureDetector
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by david.schnepp on 17/07/2017.
 */
abstract class MainActivityBase : AppCompatActivity(){

    companion object Static {
        val REQUEST_IMAGE_CAPTURE = 1
        val REQUEST_TAKE_PHOTO = 1
        val DEBUG_TAG = "Main"
    }
    var currentPhotoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

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
            startActivityForResult(takePictureIntent, MainActivity.REQUEST_TAKE_PHOTO)
        }?: Snackbar.make(ivUserPicture, "No photo app installed", Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    fun deletePreviousFile(){
        val fileUri = "file://" + currentPhotoPath
        val file = File(fileUri)
        file.delete()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MainActivity.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
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

}