package com.example.meeera.imageconverter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import bolts.Continuation
import bolts.Task
import bolts.Task.call
import com.afollestad.materialdialogs.MaterialDialog
import com.gun0912.tedpicker.ImagePickerActivity
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.shockwave.pdfium.PdfiumCore
import com.vincent.filepicker.Constant
import com.vincent.filepicker.activity.NormalFilePickActivity
import com.vincent.filepicker.filter.entity.NormalFile
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import java.util.concurrent.Callable

class MainActivity : AppCompatActivity() {

    private val INTENT_REQUEST_GET_IMAGES = 11
    val REQUEST_GET_PDF = 2
    val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    val REQUEST_ID_MULTIPLE_PERMISSIONS1 = 2
    val REQUEST_ID_MULTIPLE_PERMISSIONS2 = 3
    var tempUri : ArrayList<String> = ArrayList()
    var tempDocUri : ArrayList<String> = ArrayList()
    var tempPdfUri : ArrayList<String> = ArrayList()
    var docUri : ArrayList<String> = ArrayList()
    var pdfUri : ArrayList<String> = ArrayList()
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
        selectpdf.setOnClickListener({
            selectPdf()
        })
        pdfimg.setOnClickListener({
            createImage()
        })
        mergepdf.setOnClickListener({
            mergePdf()
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
                    ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(arrayOfNulls(listPermissionNeeded.size)), REQUEST_ID_MULTIPLE_PERMISSIONS)
                } else{
                var uri: ArrayList<Uri> = ArrayList(tempUri.size)
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
    }

    fun selectPdf(){
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
                    ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(arrayOfNulls(listPermissionNeeded.size)), REQUEST_ID_MULTIPLE_PERMISSIONS2)
                } else {
                    var intent = Intent(this, NormalFilePickActivity::class.java)
                    intent.putExtra(NormalFilePickActivity.SUFFIX, arrayOf("pdf"))
                    startActivityForResult(intent, REQUEST_GET_PDF)
                }
            } else {
                var intent = Intent(this, NormalFilePickActivity::class.java)
                intent.putExtra(NormalFilePickActivity.SUFFIX, arrayOf("pdf"))
                startActivityForResult(intent, REQUEST_GET_PDF)
            }
    }

    fun createImage(){
        if(tempPdfUri.size == 0){
            Toast.makeText(this, "No pdf selected", Toast.LENGTH_LONG).show()
        }else{
            pdfUri = tempPdfUri.clone() as ArrayList<String>
            MaterialDialog.Builder(this)
                    .title("Creating Pdf")
                    .content("Enter File name")
                    .input("Example : test", null, MaterialDialog.InputCallback { _, input ->
                        if (input == null || input.toString().trim().equals("")) {
                            Toast.makeText(this, "name cannot be blank", Toast.LENGTH_SHORT).show()
                        } else {
                            fileName = input.toString()
                            Log.d("docsize", "size "+pdfUri.size)
                            CreatingPdfImg(this, fileName, pdfUri).execute()
                        }
                    }).show()
        }
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

    fun mergePdf(){
        if(tempPdfUri.size == 0){
            Toast.makeText(this, "No pdf selected", Toast.LENGTH_LONG).show()
        }else{
            pdfUri = tempPdfUri.clone() as ArrayList<String>
            MaterialDialog.Builder(this)
                    .title("Merging Pdf")
                    .content("Enter File name")
                    .input("Example : test", null, MaterialDialog.InputCallback { _, input ->
                        if (input == null || input.toString().trim().equals("")) {
                            Toast.makeText(this, "name cannot be blank", Toast.LENGTH_SHORT).show()
                        } else {
                            fileName = input.toString()
                            Log.d("docsize", "size "+pdfUri.size)
                            mergePDF( fileName, pdfUri)
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
            Toast.makeText(this, "Image Added", Toast.LENGTH_SHORT).show()
        }

        if(requestCode == Constant.REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK){
            var list : ArrayList<NormalFile> = data!!.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE)
            for(i in list.indices){
                tempDocUri.add(list[i].path)
            }
            Toast.makeText(this, "Doc Added", Toast.LENGTH_SHORT).show()
        }

        if(requestCode == REQUEST_GET_PDF && resultCode == Activity.RESULT_OK){
            var list : ArrayList<NormalFile> = data!!.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE)
            for(i in list.indices){
                tempPdfUri.add(list[i].path)
            }
            Toast.makeText(this, "Pdf Added", Toast.LENGTH_SHORT).show()
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
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
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
                if (grantResults.size > 0) {
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        var intent = Intent(this, NormalFilePickActivity::class.java)
                        intent.putExtra(NormalFilePickActivity.SUFFIX, arrayOf("doc"))
                        startActivityForResult(intent, Constant.REQUEST_CODE_PICK_FILE)
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK(" Read and write external storage permission needed", DialogInterface.OnClickListener { _, which ->
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
            REQUEST_ID_MULTIPLE_PERMISSIONS2 ->{
                val perms: HashMap<String, Int> = HashMap()
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED)
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED)
                if (grantResults.size > 0) {
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        var intent = Intent(this, NormalFilePickActivity::class.java)
                        intent.putExtra(NormalFilePickActivity.SUFFIX, arrayOf("pdf"))
                        startActivityForResult(intent, REQUEST_GET_PDF)
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK(" Read and write external storage permission needed", DialogInterface.OnClickListener { _, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> {
                                        selectPdf()
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

    fun mergePDF(fileName:String, pdfUri:ArrayList<String>){
        var builder : MaterialDialog.Builder  = MaterialDialog.Builder(this)
                .title("please wait")
                .content("Creating File")
                .cancelable(false)
                .progress(true, 0)
        var dialog : MaterialDialog = builder.build()
        dialog.show()
        var task1 = Task<Boolean>(true)
        task1.callInBackground({
           var path : String = Environment.getExternalStorageDirectory().absolutePath+"/Mergepdf"
            var storageDir = File(path)
            if(!storageDir.exists()) {
                storageDir.mkdirs()
            }
            path = path+fileName+".pdf"
            var file = File(path)
            var document = Document()
            var fileOutStream = FileOutputStream(file)
            var copy = PdfCopy(document, fileOutStream)
            document.open()
            var n = 0
            for(i in 0 until pdfUri.size){
                var pr = PdfReader(pdfUri.get(i))
                n = pr.numberOfPages
                for (page in 1 until n+1){
                    copy.addPage(copy.getImportedPage(pr, page))
                }
            }
            document.close()
        }).onSuccess({
            Log.d("success ", "merged pdf")
            dialog.dismiss()
        })
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

    class CreatingPdfImg(context: Activity, fileName:String, docUri:ArrayList<String>) : AsyncTask<String, String, String>(){

        var fileName : String = fileName
        var imageUri : ArrayList<String> = docUri
        var context : Context = context.baseContext
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
            var path : String = Environment.getExternalStorageDirectory().absolutePath+"/ImagePdf"
            var storageDir = File(path)
            if(!storageDir.exists()) {
                storageDir.mkdirs()
            }

            for (i in 0 until imageUri.size){
                var file = File(imageUri[i])
                var fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                var pdfiumCore = PdfiumCore(context)
                var pdfDocument = pdfiumCore.newDocument(fileDescriptor)
                for (page in 0 until pdfiumCore.getPageCount(pdfDocument)) {
                    var pathFile = File(path, fileName + page + ".png")
                    pdfiumCore.openPage(pdfDocument, page)
                    val width = pdfiumCore.getPageWidthPoint(pdfDocument, page)
                    val height = pdfiumCore.getPageHeightPoint(pdfDocument, page)
                    var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    pdfiumCore.renderPageBitmap(pdfDocument, bitmap, page, 0, 0, width, height)
                    var out = FileOutputStream(pathFile)
                    var bos = BufferedOutputStream(out)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                    Log.d("amit1", "bitmap " + bitmap)
                    bos.flush()
                    bos.close()
                }
                pdfiumCore.closeDocument(pdfDocument)
                fileDescriptor.close()
            }
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            dialog.dismiss()
        }
    }
}
