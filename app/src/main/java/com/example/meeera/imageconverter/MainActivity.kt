package com.example.meeera.imageconverter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.gun0912.tedpicker.ImagePickerActivity
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import com.vincent.filepicker.Constant
import com.vincent.filepicker.activity.NormalFilePickActivity
import com.vincent.filepicker.filter.entity.NormalFile
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import org.apache.commons.io.FileUtils
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val INTENT_REQUEST_GET_IMAGES = 11
    val REQUEST_GET_DOC = 2
    val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    val REQUEST_ID_MULTIPLE_PERMISSIONS1 = 2
    var tempUri : ArrayList<String> = ArrayList()
    var tempDocUri : ArrayList<String> = ArrayList()
    var docUri : ArrayList<String> = ArrayList()
    var imageUri : ArrayList<String> = ArrayList()
    var fileName : String = ""
    var docFilePath : String ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        select.setOnClickListener({
            selectImage()
        })
        pdf.setOnClickListener({
            createImagePdf()
        })
        doc.setOnClickListener({
            selectDoc()
        })
        docpdf.setOnClickListener({
            createDocPdf()
        })
    }

    fun selectImage(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var permissionReadStorage = ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                var permissionWriteStorage = ContextCompat.checkSelfPermission(baseContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                var permissionCamera = ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)
                var listPermissionNeeded : ArrayList<String> = ArrayList<String>()
                if(permissionWriteStorage != PackageManager.PERMISSION_GRANTED){
                    listPermissionNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if(permissionReadStorage != PackageManager.PERMISSION_GRANTED){
                    listPermissionNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                if(permissionCamera != PackageManager.PERMISSION_GRANTED){
                    listPermissionNeeded.add(Manifest.permission.CAMERA)
                }
                if(!listPermissionNeeded.isEmpty()){
                    Log.d("create2", "size "+tempUri.size)
                    ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(arrayOfNulls(listPermissionNeeded.size)), REQUEST_ID_MULTIPLE_PERMISSIONS)
                } else{
                var uri: ArrayList<Uri> = ArrayList(tempUri.size)
                    Log.d("create", "size "+tempUri.size)
                for (stringUri in tempUri) {
                    uri.add(Uri.fromFile(File(stringUri)))
                }
                intent.putExtra(ImagePickerActivity.EXTRA_IMAGE_URIS, uri)
                startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES)
            }
        } else {
            var intent = Intent(this, ImagePickerActivity::class.java)
            var uri: ArrayList<Uri> = ArrayList(tempUri.size)
            for (stringUri in tempUri) {
                uri.add(Uri.fromFile(File(stringUri)))
            }
            intent.putExtra(ImagePickerActivity.EXTRA_IMAGE_URIS, uri)
            startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES)
        }

    }

    fun createImagePdf(){
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

    fun selectDoc(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var permissionReadStorage = ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_EXTERNAL_STORAGE)
            var permissionWriteStorage = ContextCompat.checkSelfPermission(baseContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            var listPermissionNeeded: ArrayList<String> = ArrayList<String>()
            if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (!listPermissionNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(arrayOfNulls(listPermissionNeeded.size)), REQUEST_ID_MULTIPLE_PERMISSIONS1)
            } else {
                var intent = Intent(this, NormalFilePickActivity::class.java)
                intent.putExtra(NormalFilePickActivity.SUFFIX, arrayOf("doc"))
                startActivityForResult(intent, Constant.REQUEST_CODE_PICK_FILE)
            }
        } else {
            var intent = Intent(this, NormalFilePickActivity::class.java)
            intent.putExtra(NormalFilePickActivity.SUFFIX, arrayOf("doc"))
            startActivityForResult(intent, Constant.REQUEST_CODE_PICK_FILE)
        }
       /* var intent = Intent()
        intent.setType("application/msword")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select doc"), REQUEST_GET_DOC )*/
    }

    fun createDocPdf(){
        if(tempDocUri.size == 0) {
            Toast.makeText(this, "No doc selected", Toast.LENGTH_LONG).show()
        }else{
            docUri = tempDocUri.clone() as ArrayList<String>
            MaterialDialog.Builder(this)
                    .title("Creating Pdf")
                    .content("Enter File name")
                    .input("Example : test", null, MaterialDialog.InputCallback { _, input ->
                        if (input == null || input.toString().trim().equals("")) {
                            Toast.makeText(this, "name cannot be blank", Toast.LENGTH_SHORT).show()
                        } else {
                            fileName = input.toString()
                            Log.d("docsize", "size "+docUri.size)
                            CreatingDocPdf(this, fileName, docUri).execute()
                        }
                    }).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == INTENT_REQUEST_GET_IMAGES && resultCode == Activity.RESULT_OK) {
            tempUri.clear()
            var imageUri: ArrayList<Uri> = data!!.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS)
            for (i in imageUri.indices) {
                tempUri.add(imageUri[i].path)
            }
            Log.d("result","size "+tempUri.size)
            Toast.makeText(this, "Image Added", Toast.LENGTH_SHORT).show()
        }

        if(requestCode == Constant.REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK){
            var list : ArrayList<NormalFile> = data!!.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE)
            for(i in list.indices){
                tempDocUri.add(list[i].path)
            }
            Toast.makeText(this, "Doc Added", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_ID_MULTIPLE_PERMISSIONS ->{
                val perms: HashMap<String, Int> = HashMap()
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED)
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED)
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED)
                if (grantResults.size > 0) {
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        var intent = Intent(this, ImagePickerActivity::class.java)
                        var uri: ArrayList<Uri> = ArrayList(tempUri.size)
                        for (stringUri in tempUri) {
                            uri.add(Uri.fromFile(File(stringUri)))
                        }
                        intent.putExtra(ImagePickerActivity.EXTRA_IMAGE_URIS, uri)
                        startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES)
                        Log.d("permission", "size "+MainActivity().tempUri.size)
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                            Log.d("permission2", "size "+MainActivity().tempUri.size)
                            showDialogOK(" Read and write external storage and camera permission needed", DialogInterface.OnClickListener { _, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> {
                                        selectImage()
                                    }

                                    DialogInterface.BUTTON_NEGATIVE -> {
                                        Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            REQUEST_ID_MULTIPLE_PERMISSIONS1 ->{
                val perms: HashMap<String, Int> = HashMap()
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED)
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED)
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED)
                if (grantResults.size > 0) {
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        var intent = Intent(this, NormalFilePickActivity::class.java)
                        intent.putExtra(NormalFilePickActivity.SUFFIX, arrayOf("doc"))
                        startActivityForResult(intent, Constant.REQUEST_CODE_PICK_FILE)
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                            showDialogOK(" Read and write external storage and camera permission needed", DialogInterface.OnClickListener { _, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> {
                                        selectDoc()
                                    }

                                    DialogInterface.BUTTON_NEGATIVE -> {
                                        Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    fun showDialogOK(message : String, okListener : DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show()
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
            Log.d("post execute2", "size "+MainActivity().tempUri.size)
            MainActivity().imageUri.clear()
            MainActivity().tempUri.clear()
            Log.d("post execute", "size "+MainActivity().tempUri.size)
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
                    //var bmp = compressImage(imageUri[i])
                    var bmp = MediaStore.Images.Media.getBitmap(context1.contentResolver,
                            Uri.fromFile(File(imageUri[i])))
                    bmp.compress(Bitmap.CompressFormat.PNG, 70, ByteArrayOutputStream())

                    //var image = Image.getInstance(imageUri[i])
                    var image = Image.getInstance(compressImage(imageUri[i]))
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

        fun compressImage(imageUri: String) : ByteArray?{

            val filePath = imageUri
            var scaledBitmap: Bitmap? = null

            val options = BitmapFactory.Options()

            //      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
            //      you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true
            var bmp = BitmapFactory.decodeFile(filePath, options)

            var actualHeight = options.outHeight
            var actualWidth = options.outWidth

            val maxHeight = 616.0f
            val maxWidth = 412.0f
            var imgRatio = (actualWidth / actualHeight).toFloat()
            val maxRatio = maxWidth / maxHeight

            //      width and height values are set maintaining the aspect ratio of the image

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight
                    actualWidth = (imgRatio * actualWidth).toInt()
                    actualHeight = maxHeight.toInt()
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth
                    actualHeight = (imgRatio * actualHeight).toInt()
                    actualWidth = maxWidth.toInt()
                } else {
                    actualHeight = maxHeight.toInt()
                    actualWidth = maxWidth.toInt()

                }
            }

            //      setting inSampleSize value allows to load a scaled down version of the original image

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

            //      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false

            //      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true
            options.inInputShareable = true
            options.inTempStorage = ByteArray(16 * 1024)

            try {
                //          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(filePath, options)
            } catch (exception: OutOfMemoryError) {
                exception.printStackTrace()

            }

            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
            } catch (exception: OutOfMemoryError) {
                exception.printStackTrace()
            }

            val ratioX = actualWidth / options.outWidth.toFloat()
            val ratioY = actualHeight / options.outHeight.toFloat()
            val middleX = actualWidth / 2.0f
            val middleY = actualHeight / 2.0f

            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

            val canvas = Canvas(scaledBitmap!!)
            canvas.matrix = scaleMatrix
            canvas.drawBitmap(bmp, middleX - bmp.width / 2, middleY - bmp.height / 2, Paint(Paint.FILTER_BITMAP_FLAG))

            //      check the rotation of the image and display it properly
            val exif: ExifInterface
            try {
                exif = ExifInterface(filePath)

                val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0)
                Log.d("EXIF", "Exif: " + orientation)
                val matrix = Matrix()
                if (orientation == 6) {
                    matrix.postRotate(90f)
                    Log.d("EXIF", "Exif: " + orientation)
                } else if (orientation == 3) {
                    matrix.postRotate(180f)
                    Log.d("EXIF", "Exif: " + orientation)
                } else if (orientation == 8) {
                    matrix.postRotate(270f)
                    Log.d("EXIF", "Exif: " + orientation)
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.width, scaledBitmap.height, matrix,
                        true)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            var stream: ByteArrayOutputStream? = ByteArrayOutputStream()
            scaledBitmap?.compress(Bitmap.CompressFormat.PNG, 85, stream)
            var bytearray = stream?.toByteArray()
            try {
                stream?.close()
            } catch (e: IOException) {

                e.printStackTrace()
            }
            return  bytearray
        }

       /* private fun getRealPathFromURI(contentUri: Uri): String {
            // Uri contentUri = Uri.parse(contentURI);
            val cursor = contentResolver.query(contentUri, null, null, null, null)
            if (cursor == null) {
                return contentUri.path
            } else {
                cursor.moveToFirst()
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                return cursor.getString(index)
            }
        }*/

        fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
                inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            }
            val totalPixels = (width * height).toFloat()
            val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++
            }

            return inSampleSize
        }
    }

    class CreatingDocPdf(context: Activity, fileName:String, docUri:ArrayList<String>) : AsyncTask<String, String, String>() {
        var context1 : Context = context.baseContext
        var fileName : String = fileName
        var imageUri : ArrayList<String> = docUri
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

        override fun doInBackground(vararg params: String?): String {
            var path : String = Environment.getExternalStorageDirectory().absolutePath+"/PDFfiles"
            path = path + fileName + ".pdf"
            var document = Document(PageSize.A4, 35f, 35f, 50f, 35f)
            var writer = PdfWriter.getInstance(document, FileOutputStream(path))
            document.open()
            try {
                for(i in 0 until imageUri.size){
                    document.add(Paragraph(org.apache.commons.io.FileUtils.readFileToString(File(imageUri[i]), "UTF-8")))
                    document.newPage()
                }
                document.close()
            }catch(e : Exception){
                e.printStackTrace()
                document.close()
            }
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            MainActivity().docUri.clear()
            MainActivity().tempDocUri.clear()
            dialog.dismiss()
        }
    }
}
