package com.orestpalii.diploma.ui.helper

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.orestpalii.diploma.data.model.Post
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

@Composable
fun PostRowView(post: Post, onClick: () -> Unit) {
    var imageLoaded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(4.dp)
            ) {
                val context = LocalContext.current

                AndroidView(
                    factory = { ctx ->
                        ImageView(ctx).apply {
                            scaleType    = ImageView.ScaleType.CENTER_CROP
                            clipToOutline= true
                            background   = null
                        }
                    },
                    update = { imageView ->
                        if (post.imageUrl.isNotEmpty()) {
                            Picasso.get()
                                .load(post.imageUrl)
                                .resize(1024, 1024)
                                .onlyScaleDown()
                                .centerCrop()
                                .transform(ExifRotationTransformation(post.imageUrl))
                                .into(imageView, object : Callback {
                                    override fun onSuccess() {
                                        imageLoaded = true
                                    }
                                    override fun onError(e: Exception?) {
                                        imageLoaded = true
                                    }
                                })
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                topStart     = 20.dp,
                                topEnd       = 20.dp,
                                bottomStart  = 10.dp,
                                bottomEnd    = 10.dp
                            )
                        )
                )
            }

            if (post.title.isNotEmpty() || post.isAIgenerated) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (post.title.isNotEmpty()) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (post.isAIgenerated) {
                        Text(
                            text = "AI",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (post.likesCount > 0 || !post.comments.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (post.likesCount > 0) {
                        IconText(icon = Icons.Default.Favorite, text = post.likesCount.toString())
                    }
                    if (!post.comments.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconText(icon = Icons.AutoMirrored.Filled.Message, text = post.comments!!.size.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun IconText(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(15.dp).width(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
