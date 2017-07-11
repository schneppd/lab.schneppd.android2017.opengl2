package com.schneppd.myopenglapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.imageBitmap

class MainActivity : AppCompatActivity() {

	companion object Static {
		val REQUEST_IMAGE_CAPTURE = 1
	}

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
	}

	fun onTakePhoto() {
		//Snackbar.make(v, "Taking photo for background", Snackbar.LENGTH_LONG).setAction("Action", null).show()
		val takePictureIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		val serviceProvider = takePictureIntent.resolveActivity(packageManager)
		serviceProvider?.let {
			startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
		}?: Snackbar.make(ivUserPicture, "No photo app installed", Snackbar.LENGTH_LONG).setAction("Action", null).show()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			val extras = data!!.extras
			val rawImageBitmap = extras.get("data") as Bitmap
			val imageBitmap = Bitmap.createScaledBitmap(rawImageBitmap, ivUserPicture.width, ivUserPicture.height, true)
			ivUserPicture.imageBitmap = imageBitmap
		}
		super.onActivityResult(requestCode, resultCode, data)
	}
}
