package android.net

import android.os.Parcel

class FakeUri(
    private val fakeScheme: String?,
    private val fakeHost: String?,
    private val fakePathSegments: List<String> = emptyList()
) : Uri() {
    override fun isHierarchical(): Boolean = true

    override fun isRelative(): Boolean = fakeScheme == null

    override fun getScheme(): String? = fakeScheme

    override fun getSchemeSpecificPart(): String? = null

    override fun getEncodedSchemeSpecificPart(): String? = null

    override fun getAuthority(): String? = fakeHost

    override fun getEncodedAuthority(): String? = fakeHost

    override fun getUserInfo(): String? = null

    override fun getEncodedUserInfo(): String? = null

    override fun getHost(): String? = fakeHost

    override fun getPort(): Int = -1

    override fun getPath(): String = fakePathSegments.joinToString(prefix = "/", separator = "/")

    override fun getEncodedPath(): String = path

    override fun getQuery(): String? = null

    override fun getEncodedQuery(): String? = null

    override fun getFragment(): String? = null

    override fun getEncodedFragment(): String? = null

    override fun getPathSegments(): List<String> = fakePathSegments

    override fun getLastPathSegment(): String? = fakePathSegments.lastOrNull()

    override fun buildUpon(): Builder {
        throw UnsupportedOperationException("FakeUri does not support builders.")
    }

    override fun toString(): String {
        return buildString {
            fakeScheme?.let { append(it).append("://") }
            fakeHost?.let { append(it) }
            append(path)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = Unit
}
