package org.russelltg.bridge

import android.content.ContentUris
import android.graphics.Bitmap
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Telephony
import android.util.Base64
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.ByteArrayOutputStream

class GetContactInfo(serv: ServerService) : Command(serv) {

    override fun process(params: JsonElement): JsonElement? {
        val contactID = params.asInt

        // get the canonical address
        val cr = service.contentResolver


        // query
        var contactsCursor = cr.query(Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, contactID.toString()),
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null)

        if (contactsCursor.moveToFirst()) {

            val name = contactsCursor.getString(0)

            var base64image = ""
            if (!contactsCursor.isNull(1)) {

                val image_uri = Uri.parse(contactsCursor.getString(1))

                var bm = MediaStore.Images.Media.getBitmap(cr, image_uri)

                // encode as jpeg
                var stream = ByteArrayOutputStream()
                bm.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                // base64 encode it
                base64image = "data:image/jpeg;base64, " + Base64.encodeToString(stream.toByteArray(), 0)

            }

            val json = JsonObject()

            json["name"] = name
            json["b64_image"] = base64image

            return json


        }

        // we can at least return number
        val json = JsonObject()

        json["name"] = ""
        json["b64_image"] = ""
        return json

    }
}