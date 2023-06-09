package com.axyz.upasthithguru.activity

//import com.axyz.upasthithguru.adapters.studentAttendanceListAdapter
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axyz.upasthithg.Realm.ClassAttendanceManager
import com.axyz.upasthithguru.adapters.studentAttendanceListAdapter
import com.axyz.upasthithguru.databinding.ActivityViewStudentAttendanceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId

//import com.axyz.upasthithguru.adapters.AttendanceAdapter

// These Activity shows the dates of attendance record for the teacher
class ViewStudentAttendance : AppCompatActivity() {
    private lateinit var binding: ActivityViewStudentAttendanceBinding
    private lateinit var rvAttendance: RecyclerView
    private lateinit var courseId: ObjectId
    private lateinit var studentAttendanceListAdapter: studentAttendanceListAdapter
    private lateinit var date: TextView
    private lateinit var studentCount:TextView
    private lateinit var classNumber:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStudentAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        classNumber = intent?.getStringExtra("Class Number").toString()
        Log.d(TAG,classNumber)
        courseId = intent.getByteArrayExtra("Student Course Attendance Id")?.let { ObjectId(it) }!!
        date = binding.viewAttendanceDate
        date.text = intent.getStringExtra("Attendance Date").toString()
        studentCount = binding.viewStudentNumberOfStudents
        setupRecyclerView()
        loadAttendanceData()
        setupAddAttendanceButton()
    }

    private fun setupRecyclerView() {
        rvAttendance = binding.rvAttendance
        rvAttendance.layoutManager = LinearLayoutManager(this)
        rvAttendance.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun loadAttendanceData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val attendanceRecords = ClassAttendanceManager().getAllStudentRecordsOfParticularDate(courseId.toHexString(),date.text.toString())
                attendanceRecords?.let {
                    withContext(Dispatchers.Main) {
                        rvAttendance.adapter = studentAttendanceListAdapter(attendanceRecords)
                        studentAttendanceListAdapter.notifyDataSetChanged()
                        studentCount.text = attendanceRecords.size.toString() +"/80"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading attendance data: $e")
            }
        }
    }

    private fun setupAddAttendanceButton() {
        binding.viewStudentAddStudents.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ClassAttendanceManager().addStudentRecord(courseId, classNumber.toInt(), "Yash@gmail.com")
                    loadAttendanceData()
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding attendance record: $e")
                }
            }
        }
    }
}
