/* ###
 * IP: GHIDRA
 * REVIEWED: YES
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
 */
package ghidra.program.database.bookmark;

import java.io.IOException;

import db.DBHandle;
import db.Record;

/**
 * 
 */
public class BookmarkTypeDBAdapterNoTable extends BookmarkTypeDBAdapter {

	private Record[] records = new Record[0];

	/**
	 * @param dbHandle
	 * @param openMode
	 */
	public BookmarkTypeDBAdapterNoTable(DBHandle dbHandle) {
	}

	/**
	 * Set the old bookmark manager which handles read-only access
	 * to bookmarks stored within property maps.
	 * The old bookmark manager must be set prior to invoking any other method;
	 * @param oldMgr old bookmark manager
	 */
	public void setOldBookmarkManager(OldBookmarkManager oldMgr) {
		records = oldMgr.getTypeRecords();
	}

	/* (non-Javadoc)
	 * @see ghidra.program.database.bookmark.BookmarkTypeDBAdapter#getRecords()
	 */
	@Override
	Record[] getRecords() throws IOException {
		return records;
	}

}
