package com.simplemobiletools.contacts.extensions

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.support.v4.content.FileProvider
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getIntValue
import com.simplemobiletools.commons.extensions.isLollipopPlus
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.contacts.BuildConfig
import com.simplemobiletools.contacts.activities.ContactActivity
import com.simplemobiletools.contacts.helpers.CONTACT_ID
import com.simplemobiletools.contacts.helpers.Config
import com.simplemobiletools.contacts.models.Contact
import java.io.File

val Context.config: Config get() = Config.newInstance(applicationContext)

fun Context.openContact(contact: Contact) {
    Intent(applicationContext, ContactActivity::class.java).apply {
        putExtra(CONTACT_ID, contact.id)
        startActivity(this)
    }
}

fun Context.sendEmailIntent(recipient: String) {
    Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.fromParts("mailto", recipient, null)
        if (resolveActivity(packageManager) != null) {
            startActivity(this)
        } else {
            toast(R.string.no_app_found)
        }
    }
}

fun Context.sendSMSIntent(recipient: String) {
    Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.fromParts("smsto", recipient, null)
        if (resolveActivity(packageManager) != null) {
            startActivity(this)
        } else {
            toast(R.string.no_app_found)
        }
    }
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun Context.getLookupUriRawId(dataUri: Uri): Int {
    val lookupKey = getLookupKeyFromUri(dataUri)
    if (lookupKey != null && isLollipopPlus()) {
        val uri = lookupContactUri(lookupKey, this)
        return getContactUriRawId(uri)
    }
    return -1
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun Context.getContactUriRawId(uri: Uri): Int {
    val projection = arrayOf(ContactsContract.Contacts.NAME_RAW_CONTACT_ID)
    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor.moveToFirst()) {
            return cursor.getIntValue(ContactsContract.Contacts.NAME_RAW_CONTACT_ID)
        }
    } finally {
        cursor?.close()
    }
    return -1
}

// from https://android.googlesource.com/platform/packages/apps/Dialer/+/68038172793ee0e2ab3e2e56ddfbeb82879d1f58/java/com/android/contacts/common/util/UriUtils.java
fun getLookupKeyFromUri(lookupUri: Uri): String? {
    return if (!isEncodedContactUri(lookupUri)) {
        val segments = lookupUri.pathSegments
        if (segments.size < 3) null else Uri.encode(segments[2])
    } else {
        null
    }
}

fun isEncodedContactUri(uri: Uri?): Boolean {
    if (uri == null) {
        return false
    }
    val lastPathSegment = uri.lastPathSegment ?: return false
    return lastPathSegment == "encoded"
}

fun lookupContactUri(lookup: String, context: Context): Uri {
    val lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookup)
    return ContactsContract.Contacts.lookupContact(context.contentResolver, lookupUri)
}

fun Context.getCachePhoto(): File {
    val imagesFolder = File(cacheDir, "my_cache")
    if (!imagesFolder.exists()) {
        imagesFolder.mkdirs()
    }

    val file = File(imagesFolder, "Photo_${System.currentTimeMillis()}.jpg")
    file.createNewFile()
    return file
}

fun Context.getCachePhotoUri(file: File = getCachePhoto()) = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)
