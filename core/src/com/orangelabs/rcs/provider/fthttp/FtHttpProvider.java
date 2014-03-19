/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.orangelabs.rcs.provider.fthttp;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.orangelabs.rcs.provider.RichProviderHelper;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * @author YPLO6403
 *
 * Provider for the FT HTTP table
 */
public class FtHttpProvider extends ContentProvider {
	private static final String TYPE_CURSOR_ITEM = "vnd.android.cursor.item/";
	private static final String TYPE_CURSOR_DIR = "vnd.android.cursor.dir/";

	public static final String AUTHORITY = "com.orangelabs.rcs.fthttp";
	public static final String CONTENT_URI_BASE = "content://" + AUTHORITY;

	public static final String QUERY_NOTIFY = "QUERY_NOTIFY";
	public static final String QUERY_GROUP_BY = "QUERY_GROUP_BY";

	private static final int URI_TYPE_FTHTTP = 0;
	private static final int URI_TYPE_FTHTTP_ID = 1;

	/**
	 * The logger
	 */
	final private static Logger logger = Logger.getLogger(FtHttpProvider.class.getSimpleName());

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		URI_MATCHER.addURI(AUTHORITY, FtHttpColumns.TABLE, URI_TYPE_FTHTTP);
		URI_MATCHER.addURI(AUTHORITY, FtHttpColumns.TABLE + "/#", URI_TYPE_FTHTTP_ID);
	}

	private RichProviderHelper mRichProviderHelper;

	@Override
	public boolean onCreate() {
		if (RichProviderHelper.getInstance() == null) {
			RichProviderHelper.createInstance(this.getContext());
		}
		mRichProviderHelper = RichProviderHelper.getInstance();
		return true;
	}

	@Override
	public String getType(Uri uri) {
		final int match = URI_MATCHER.match(uri);
		switch (match) {
		case URI_TYPE_FTHTTP:
			return TYPE_CURSOR_DIR + FtHttpColumns.TABLE;
		case URI_TYPE_FTHTTP_ID:
			return TYPE_CURSOR_ITEM + FtHttpColumns.TABLE;

		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (logger.isActivated())  {
			logger.debug("insert uri=" + uri + " values=" + values );
		}
		final String table = uri.getLastPathSegment();
		final long rowId = mRichProviderHelper.getWritableDatabase().insert(table, null, values);
		String notify;
		if (rowId != -1 && ((notify = uri.getQueryParameter(QUERY_NOTIFY)) == null || "true".equals(notify))) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return uri.buildUpon().appendEncodedPath(String.valueOf(rowId)).build();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (logger.isActivated()) {
			logger.debug("update uri=" + uri + " values=" + values + " selection=" + selection + " selectionArgs="
					+ Arrays.toString(selectionArgs));
		}
		final QueryParams queryParams = getQueryParams(uri, selection);
		final int res = mRichProviderHelper.getWritableDatabase().update(queryParams.table, values, queryParams.selection,
				selectionArgs);
		String notify;
		if (res != 0 && ((notify = uri.getQueryParameter(QUERY_NOTIFY)) == null || "true".equals(notify))) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return res;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (logger.isActivated()) {
			logger.debug("delete uri=" + uri + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs));
		}
		final QueryParams queryParams = getQueryParams(uri, selection);
		final int res = mRichProviderHelper.getWritableDatabase().delete(queryParams.table, queryParams.selection, selectionArgs);
		String notify;
		if (res != 0 && ((notify = uri.getQueryParameter(QUERY_NOTIFY)) == null || "true".equals(notify))) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return res;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final String groupBy = uri.getQueryParameter(QUERY_GROUP_BY);
		final QueryParams queryParams = getQueryParams(uri, selection);
		final Cursor res = mRichProviderHelper.getReadableDatabase().query(queryParams.table, projection, queryParams.selection,
				selectionArgs, groupBy, null, sortOrder == null ? queryParams.orderBy : sortOrder);
		if (logger.isActivated()) {
			logger.debug("query uri=" + uri + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs)
					+ " sortOrder=" + (sortOrder == null ? queryParams.orderBy : sortOrder) + " groupBy=" + groupBy);
		}
		res.setNotificationUri(getContext().getContentResolver(), uri);
		return res;
	}

	private static class QueryParams {
		public String table;
		public String selection;
		public String orderBy;
	}

	private QueryParams getQueryParams(Uri uri, String selection) {
		QueryParams res = new QueryParams();
		String id = null;
		int matchedId = URI_MATCHER.match(uri);
		switch (matchedId) {
		case URI_TYPE_FTHTTP:
		case URI_TYPE_FTHTTP_ID:
			res.table = FtHttpColumns.TABLE;
			res.orderBy = FtHttpColumns.DEFAULT_ORDER;
			break;

		default:
			throw new IllegalArgumentException("The uri '" + uri + "' is not supported by this ContentProvider");
		}

		switch (matchedId) {
		case URI_TYPE_FTHTTP_ID:
			id = uri.getLastPathSegment();
		}
		if (id != null) {
			if (selection != null) {
				res.selection = BaseColumns._ID + "=" + id + " and (" + selection + ")";
			} else {
				res.selection = BaseColumns._ID + "=" + id;
			}
		} else {
			res.selection = selection;
		}
		return res;
	}

	public static Uri notify(Uri uri, boolean notify) {
		return uri.buildUpon().appendQueryParameter(QUERY_NOTIFY, String.valueOf(notify)).build();
	}

	public static Uri groupBy(Uri uri, String groupBy) {
		return uri.buildUpon().appendQueryParameter(QUERY_GROUP_BY, groupBy).build();
	}
}
