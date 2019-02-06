package com.psoffritti.slidingpanel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.psoffritti.librarysampleapptemplate.core.Constants
import com.psoffritti.librarysampleapptemplate.core.SampleAppTemplateActivity
import com.psoffritti.librarysampleapptemplate.core.utils.ExampleActivityDetails
import com.psoffritti.slidingpanel.examples.advanced.AdvancedExampleActivity
import com.psoffritti.slidingpanel.examples.basic.BasicExampleActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, SampleAppTemplateActivity::class.java)

        intent.putExtra(Constants.TITLE.name, getString(R.string.app_name))
        intent.putExtra(Constants.GITHUB_URL.name, "https://github.com/PierfrancescoSoffritti/sliding-panel")

        val examples = arrayOf(
            ExampleActivityDetails(
                R.string.basic_example_activity,
                null,
                BasicExampleActivity::class.java
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
