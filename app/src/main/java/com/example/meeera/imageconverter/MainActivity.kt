package com.example.meeera.imageconverter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.gun0912.tedpicker.ImagePickerActivity
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val INTENT_REQUEST_GET_IMAGES = 11
    var tempUri : ArrayList<String> = ArrayList()
    var imageUri : ArrayList<String> = ArrayList()
    var fileName : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        select.setOnClickListener({
            selectImage()
        })
        pdf.setOnClickListener({
            createPdf()
        })
    }

    fun selectImage(){
        var intent = Intent(this, ImagePickerActivity::class.java)
        var uri : ArrayList<Uri> = ArrayList(tempUri.size)
        for (stringUri in tempUri){
            uri.add(Uri.fromFile(File(stringUri)))
        }
        intent.putExtra(ImagePickerActivity.EXTRA_IMAGE_URIS, uri)
        startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES)
    }

    fun createPdf(){
        if(imageUri.size == 0){
            if(tempUri.size == 0){
                Toast.makeText(this, "No images selected", Toast.LENGTH_LONG).show()
                return
            } else{
                imageUri = tempUri.clone() as ArrayList<String>
            }
        }
     MaterialDialog.Builder(this)
             .title("Creating Pdf")
             .content("Enter File name")
             .input("Example : test", null, MaterialDialog.InputCallback { _, input ->
                 if(input == null || input.toString().trim().equals("")){
                     Toast.makeText(this, "name cannot be blank", Toast.LENGTH_SHORT).show()
                 }else{
                     fileName = input.toString()
                     Log.d("amituri", "amit"+imageUri.size)
                     CreatingPdf(this, fileName, imageUri).execute()
                 }
             }).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == INTENT_REQUEST_GET_IMAGES && resultCode == Activity.RESULT_OK) {
            tempUri.clear()
            var imageUri: ArrayList<Uri> = data.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS)
            for (i in imageUri.indices) {
                tempUri.add(imageUri[i].path)
            }
            Toast.makeText(this, "Image Added", Toast.LENGTH_SHORT).show()
        }
    }

    class CreatingPdf(context: Activity, fileName:String, imageUri:ArrayList<String>) : AsyncTask<String, String, String>() {
        var context1 : Context = context.baseContext
        var fileName : String = fileName
        var imageUri : ArrayList<String> = imageUri
        var builder : MaterialDialog.Builder  = MaterialDialog.Builder(context)
                .title("please wait")
                .content("Creating File")
                .cancelable(false)
                .progress(true, 0)
        var dialog : MaterialDialog = builder.build()

        override fun onPreExecute() {
            super.onPreExecute()
            dialog.show()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            MainActivity().imageUri.clear()
            MainActivity().tempUri.clear()
            dialog.dismiss()
        }

        override fun doInBackground(vararg params: String?): String {
            var path : String = Environment.getExternalStorageDirectory().absolutePath+"/PDFfiles"
            var folder = File(path)
            if(!folder.exists()){
                var success = folder.mkdir()
                if(!success){
                    Toast.makeText(context1, " ", Toast.LENGTH_SHORT).show()
                    return ""
                }
            }
            path = path + fileName + ".pdf"

            var document = Document(PageSize.A4, 35f, 35f, 50f, 35f)
            var documentRect = document.pageSize
            var writer = PdfWriter.getInstance(document, FileOutputStream(path))
            document.open()
            try {
                for (i in 0 until imageUri.size ) {
                    var bmp = MediaStore.Images.Media.getBitmap(context1.contentResolver,
                            Uri.fromFile(File(imageUri[i])))
                    bmp.compress(Bitmap.CompressFormat.PNG, 70, ByteArrayOutputStream())
                    var image = Image.getInstance(imageUri[i])
                    if (bmp.width > documentRect.width || bmp.height > documentRect.height) {
                        image.scaleAbsolute(documentRect.width, documentRect.height)
                    } else {
                        image.scaleAbsolute(bmp.width.toFloat(), bmp.height.toFloat())
                    }
                    image.setAbsolutePosition((documentRect.width - image.scaledWidth) / 2, (documentRect.height - image.scaledHeight) / 2)
                    image.border = Image.BOX
                    image.borderWidth = 15f
                    document.add(image)
                    document.newPage()
                }
                document.close()
            } catch (e:Exception){
                e.printStackTrace()
                document.close()
            }
            return  ""
        }

    }
}
