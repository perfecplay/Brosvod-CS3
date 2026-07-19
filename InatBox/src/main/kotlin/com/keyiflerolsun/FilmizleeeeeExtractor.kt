package com.keyiflerolsun

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.newExtractorLink

class FilmizleeeeeExtractor : ExtractorApi() {
    override val name = "Filmizleeeee"
    override val mainUrl = "https://embed.filmizleeeee.cfd"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val response = app.get(url, referer = referer ?: mainUrl)
        if (!response.isSuccessful || response.text.isBlank()) return

        callback(
            newExtractorLink(
                source = name,
                name = name,
                url = response.text.trim(),
                type = ExtractorLinkType.VIDEO
            ) {
                this.referer = referer ?: mainUrl
            }
        )
    }
}
