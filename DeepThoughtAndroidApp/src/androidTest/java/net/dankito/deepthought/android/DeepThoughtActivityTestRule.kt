package net.dankito.deepthought.android

import android.app.Activity
import android.support.test.rule.ActivityTestRule


class DeepThoughtActivityTestRule<T : Activity>(activityClass: Class<T>) : ActivityTestRule<T>(activityClass) {
}