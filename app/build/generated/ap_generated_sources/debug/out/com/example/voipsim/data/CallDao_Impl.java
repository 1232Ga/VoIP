package com.example.voipsim.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CallDao_Impl implements CallDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CallLog> __insertionAdapterOfCallLog;

  public CallDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCallLog = new EntityInsertionAdapter<CallLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `CallLog` (`id`,`caller`,`startTime`,`endTime`,`type`,`duration`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final CallLog entity) {
        statement.bindLong(1, entity.id);
        if (entity.caller == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.caller);
        }
        statement.bindLong(3, entity.startTime);
        statement.bindLong(4, entity.endTime);
        if (entity.type == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.type);
        }
        statement.bindLong(6, entity.duration);
      }
    };
  }

  @Override
  public long insert(final CallLog log) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfCallLog.insertAndReturnId(log);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<CallLog> getAll() {
    final String _sql = "SELECT * FROM CallLog ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfCaller = CursorUtil.getColumnIndexOrThrow(_cursor, "caller");
      final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
      final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
      final List<CallLog> _result = new ArrayList<CallLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final CallLog _item;
        _item = new CallLog();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfCaller)) {
          _item.caller = null;
        } else {
          _item.caller = _cursor.getString(_cursorIndexOfCaller);
        }
        _item.startTime = _cursor.getLong(_cursorIndexOfStartTime);
        _item.endTime = _cursor.getLong(_cursorIndexOfEndTime);
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        _item.duration = _cursor.getLong(_cursorIndexOfDuration);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
