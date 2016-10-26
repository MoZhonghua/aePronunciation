package com.aepronunciation.ipa;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseAdapter {

	MyDatabaseHelper helper;

	// Constructor
	public MyDatabaseAdapter(Context context) {

		helper = new MyDatabaseHelper(context);
	}

	public ArrayList<Test> getAllTestScores() {

		ArrayList<Test> allTests = new ArrayList<Test>();

		SQLiteDatabase db = helper.getWritableDatabase();
		String[] columns = { MyDatabaseHelper.ID, MyDatabaseHelper.TEST_DATE,
				MyDatabaseHelper.SCORE, MyDatabaseHelper.WRONG };
		String orderBy = MyDatabaseHelper.TEST_DATE + " DESC";
		Cursor cursor = db.query(MyDatabaseHelper.TESTS_TABLE_NAME, columns,
				null, null, null, null, orderBy, null);
		int indexId = cursor.getColumnIndex(MyDatabaseHelper.ID);
		int indexDate = cursor.getColumnIndex(MyDatabaseHelper.TEST_DATE);
		int indexScore = cursor.getColumnIndex(MyDatabaseHelper.SCORE);
		int indexWrong = cursor.getColumnIndex(MyDatabaseHelper.WRONG);

		while (cursor.moveToNext()) {
			Test test = new Test();
			test.setId(cursor.getLong(indexId));
			test.setDate(cursor.getLong(indexDate));
			test.setScore(cursor.getInt(indexScore));
			test.setWrong(cursor.getString(indexWrong));
			allTests.add(test);
		}

		// iterate over cursor in reverse order
		/*
		 * for (cursor.moveToLast(); !cursor.isBeforeFirst();
		 * cursor.moveToPrevious()) { TestList test = new TestList();
		 * test.setDate(cursor.getLong(indexDate));
		 * test.setScore(cursor.getInt(indexScore));
		 * test.setWrong(cursor.getString(indexWrong)); allTests.add(test); }
		 */

		cursor.close();
		db.close();

		return allTests;
	}

	public Test getTest(long rowId) {

		Test test = new Test();

		SQLiteDatabase db = helper.getWritableDatabase();
		String[] columns = { MyDatabaseHelper.ID, MyDatabaseHelper.USER_NAME,
				MyDatabaseHelper.TEST_DATE, MyDatabaseHelper.TIME_LENGTH,
				MyDatabaseHelper.TEST_MODE, MyDatabaseHelper.SCORE,
				MyDatabaseHelper.CORRECT_ANSWER, MyDatabaseHelper.USER_ANSWER,
				MyDatabaseHelper.WRONG };
		String whereClause = MyDatabaseHelper.ID + " =?";
		String[] whereArgs = { Long.toString(rowId) };
		Cursor cursor = db.query(MyDatabaseHelper.TESTS_TABLE_NAME, columns,
				whereClause, whereArgs, null, null, null, null);
		
		int indexId = cursor.getColumnIndex(MyDatabaseHelper.ID);
		int indexName = cursor.getColumnIndex(MyDatabaseHelper.USER_NAME);
		int indexDate = cursor.getColumnIndex(MyDatabaseHelper.TEST_DATE);
		int indexTime = cursor.getColumnIndex(MyDatabaseHelper.TIME_LENGTH);
		int indexMode = cursor.getColumnIndex(MyDatabaseHelper.TEST_MODE);
		int indexScore = cursor.getColumnIndex(MyDatabaseHelper.SCORE);
		int indexCorrect = cursor.getColumnIndex(MyDatabaseHelper.CORRECT_ANSWER);
		int indexUserAnswer = cursor.getColumnIndex(MyDatabaseHelper.USER_ANSWER);
		int indexWrong = cursor.getColumnIndex(MyDatabaseHelper.WRONG);

		while (cursor.moveToNext()) {
			test.setId(cursor.getLong(indexId));
			test.setUserName(cursor.getString(indexName));
			test.setDate(cursor.getLong(indexDate));
			test.setTimeLength(cursor.getLong(indexTime));
			test.setMode(cursor.getString(indexMode));
			test.setScore(cursor.getInt(indexScore));
			test.setCorrectAnswers(cursor.getString(indexCorrect));
			test.setUserAnswers(cursor.getString(indexUserAnswer));
			test.setWrong(cursor.getString(indexWrong));
		}

		cursor.close();
		db.close();

		return test;
	}

	public int[] getHighScores() {

		int[] highScores = new int[2];

		SQLiteDatabase db = helper.getWritableDatabase();
		String[] columns = { MyDatabaseHelper.SCORE, MyDatabaseHelper.TEST_MODE };
		String orderBy = MyDatabaseHelper.SCORE + " DESC";
		Cursor cursor = db.query(MyDatabaseHelper.TESTS_TABLE_NAME, columns,
				null, null, null, null, orderBy, null);
		int indexScore = cursor.getColumnIndex(MyDatabaseHelper.SCORE);
		int indexMode = cursor.getColumnIndex(MyDatabaseHelper.TEST_MODE);

		boolean foundDouble = false;
		boolean foundSingle = false;
		while (cursor.moveToNext()) {

			// since the results are ordered DESC then the first match is the
			// high score

			if (!foundSingle && !cursor.getString(indexMode).equals("double")) {

				highScores[0] = cursor.getInt(indexScore);
				foundSingle = true;

			} else if (!foundDouble
					&& cursor.getString(indexMode).equals("double")) {

				highScores[1] = cursor.getInt(indexScore);
				foundDouble = true;
			}

			if (foundSingle && foundDouble) {
				break;
			}
		}

		cursor.close();
		db.close();

		return highScores;
	}

	public ArrayList<String> getAllUsers() {

		ArrayList<String> results = new ArrayList<String>();
		SQLiteDatabase db = helper.getReadableDatabase();
		String[] columns = { MyDatabaseHelper.USER_NAME };
		Cursor cursor = db.query(true, MyDatabaseHelper.TESTS_TABLE_NAME,
				columns, null, null, null, null, null, null);

		int wordIndex = cursor.getColumnIndex(MyDatabaseHelper.USER_NAME);

		String tempString;
		while (cursor.moveToNext()) {

			tempString = cursor.getString(wordIndex);
			results.add(tempString);
		}

		cursor.close();
		db.close();
		return results;

	}

	public long addTest(String name, long time, String testmode, int score,
			String correctAnswers, String userAnswers, String wrong) {

		// get current Unix epoc time in milliseconds
		long date = System.currentTimeMillis();

		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(MyDatabaseHelper.USER_NAME, name);
		contentValues.put(MyDatabaseHelper.TEST_DATE, date);
		contentValues.put(MyDatabaseHelper.TIME_LENGTH, time);
		contentValues.put(MyDatabaseHelper.TEST_MODE, testmode);
		contentValues.put(MyDatabaseHelper.SCORE, score);
		contentValues.put(MyDatabaseHelper.CORRECT_ANSWER, correctAnswers);
		contentValues.put(MyDatabaseHelper.USER_ANSWER, userAnswers);
		contentValues.put(MyDatabaseHelper.WRONG, wrong);
		long id = db.insert(MyDatabaseHelper.TESTS_TABLE_NAME, null,
				contentValues);
		db.close();
		return id;
	}

	public int deleteTest(long rowId) {

		SQLiteDatabase db = helper.getWritableDatabase();
		String whereClause = MyDatabaseHelper.ID + " =?";
		String[] whereArgs = { Long.toString(rowId) };
		int count = db.delete(MyDatabaseHelper.TESTS_TABLE_NAME, whereClause,
				whereArgs);
		db.close();
		return count;
	}

	// Making this an inner class rather than a separate class so that outer
	// class can securely refer to private variables in this class
	static class MyDatabaseHelper extends SQLiteOpenHelper {

		// This creates two tables (one for now, version 2 add second)
		// In the first table, every row stores the user test scores
		// In the second table, every row stores an IPA symbol that the user got
		// wrong
		private static final String DATABASE_NAME = "aepronunciation.db";
		private static final int DATABASE_VERSION = 1;

		// //////////// Test Table //////////////////
		private static final String TESTS_TABLE_NAME = "TESTS";
		// Column names
		private static final String ID = "_id";
		private static final String USER_NAME = "name";
		private static final String TEST_DATE = "test_date";
		private static final String TIME_LENGTH = "time_length";
		private static final String TEST_MODE = "mode";
		private static final String SCORE = "score";
		private static final String CORRECT_ANSWER = "correct";
		private static final String USER_ANSWER = "user_answer";
		private static final String WRONG = "wrong";
		// SQL statements
		private static final String CREATE_TEST_TABLE = "CREATE TABLE "
				+ TESTS_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
				+ USER_NAME + " TEXT NOT NULL," + TEST_DATE + " INTEGER,"
				+ TIME_LENGTH + " INTEGER," + TEST_MODE + " TEXT NOT NULL,"
				+ SCORE + " INTEGER," + CORRECT_ANSWER + " TEXT NOT NULL,"
				+ USER_ANSWER + " TEXT NOT NULL," + WRONG + " TEXT NOT NULL)";
		private static final String DROP_TEST_TABLE = "DROP TABLE IF EXISTS "
				+ TESTS_TABLE_NAME;

		// TODO ////////////Wrong answers table //////////////////
		/*
		 * private static final String WRONG_TABLE_NAME = "WRONG"; // Column
		 * names // private static final String ID = "_id"; // private static
		 * final String USER_NAME = "name"; private static final String SYMBOL =
		 * "symbol"; private static final String FREQUENCY = "frequency";
		 * private static final String SUBSTITUTE = "substitute"; // SQL
		 * statements private static final String CREATE_WRONG_TABLE =
		 * "CREATE TABLE " + WRONG_TABLE_NAME + " (" + ID +
		 * " INTEGER PRIMARY KEY," + USER_NAME + " TEXT NOT NULL," + SYMBOL +
		 * " TEXT UNIQUE," + FREQUENCY + " INTEGER," + SUBSTITUTE +
		 * " TEXT NOT NULL)"; private static final String DROP_WRONG_TABLE =
		 * "DROP TABLE IF EXISTS " + TESTS_TABLE_NAME;
		 */

		private Context context;

		public MyDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			try {
				db.execSQL(CREATE_TEST_TABLE);
				// db.execSQL(CREATE_WRONG_TABLE);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			try {
				db.execSQL(DROP_TEST_TABLE);
				// db.execSQL(DROP_WRONG_TABLE);
				onCreate(db);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}