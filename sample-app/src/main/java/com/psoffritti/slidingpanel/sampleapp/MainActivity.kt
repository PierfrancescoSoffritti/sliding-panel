package com.psoffritti.slidingpanel.sampleapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.psoffritti.librarysampleapptemplate.core.Constants
import com.psoffritti.librarysampleapptemplate.core.SampleAppTemplateActivity
import com.psoffritti.librarysampleapptemplate.core.utils.ExampleActivityDetails
import com.psoffritti.slidingpanel.sampleapp.examples.advanced.AdvancedExampleActivity
import com.psoffritti.slidingpanel.sampleapp.examples.basic.BasicExampleHorizontalActivity
import com.psoffritti.slidingpanel.sampleapp.examples.basic.BasicExampleVerticalActivity
import com.psoffritti.slidingpanel.sampleapp.examples.dragview.DragViewExampleActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, SampleAppTemplateActivity::class.java)

        intent.putExtra(Constants.TITLE.name, getString(R.string.app_name))
        intent.putExtra(Constants.GITHUB_URL.name, "https://github.com/PierfrancescoSoffritti/sliding-panel")

        val examples = arrayOf(
            ExampleActivityDetails(
                R.string.basic_example_vertical,
                null,
                BasicExampleVerticalActivity::class.java
            ),
            ExampleActivityDetails(
                R.string.basic_example_horizontal,
                null,
                BasicExampleHorizontalActivity::class.java
            ),
            ExampleActivityDetails(
                R.string.drag_view_example,
                null,
                DragViewExampleActivity::class.java
            ),
            ExampleActivityDetails(
                R.string.advanced_example_activity,
                null,
                AdvancedExampleActivity::class.java
            )
        )

        intent.putExtra(Constants.EXAMPLES.name, examples)

        startActivity(intent)
        finish()
    }
}
