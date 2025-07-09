package org.app.service;

import org.app.model.*;
import org.app.util.DataPersistence;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Course Management Service for BUP UCAM Assignment Tracker
 */
public class CourseService {
    private Map<String, Course> courses;
    private int courseCounter;
    private DataPersistence dataPersistence;

    public CourseService() {
        this.dataPersistence = new DataPersistence();
        this.courses = new HashMap<>();  // Initialize map before loading data
        loadData();
        System.out.println("✓ CourseService initialized with persistent storage");
    }

    private void loadData() {
        DataPersistence.CourseData courseData = dataPersistence.loadCourses();
        if (courseData.courses != null) {
            this.courses = courseData.courses;
        }
        this.courseCounter = courseData.courseCounter;
        System.out.println("✓ Loaded " + courses.size() + " courses from storage");
    }

    private void saveData() {
        dataPersistence.saveCourses(courses, courseCounter);
        System.out.println("✓ Courses saved to storage");
    }

    /**
     * Create a new course
     */
    public Course createCourse(String courseName, String courseCode, String department,
                               int creditHours, String semester, Teacher instructor) {
        String courseId = "CRS-" + String.format("%04d", courseCounter++);
        Course course = new Course(courseId, courseName, courseCode, department,
                creditHours, semester, instructor);

        courses.put(courseId, course);
        instructor.addCourse(course);

        saveData(); // Save after creating course
        System.out.println("✓ Course created and saved successfully: " + courseName);
        return course;
    }

    /**
     * Enroll student in course
     */
    public void enrollStudent(String courseId, Student student) {
        Course course = courses.get(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        course.enrollStudent(student);
        saveData(); // Save after enrolling student
        System.out.println("✓ Student enrolled: " + student.getName() + " in " + course.getCourseName());
    }

    /**
     * Restore a student's enrolled courses based on their stored courseIds
     * This ensures courses persist between sessions for students
     */
    public void restoreStudentEnrollments(Student student) {
        List<String> courseIds = student.getEnrolledCourseIds();

        // Clear the current list to avoid duplicates
        student.clearEnrolledCourses();

        // For each courseId, get the actual course and add it to the student's enrolledCourses
        for (String courseId : courseIds) {
            Course course = getCourse(courseId);
            if (course != null) {
                // Re-enroll student in course without adding the ID again
                if (!course.getEnrolledStudents().contains(student)) {
                    course.enrollStudent(student);
                }
                // Make sure the student has the course in their list
                student.addEnrolledCourse(course);
            }
        }

        System.out.println("✓ Restored " + student.getEnrolledCourses().size() +
                          " courses for student " + student.getName());
    }

    /**
     * Restore a teacher's courses based on their stored courseIds
     * This ensures courses persist between sessions for teachers
     */
    public void restoreTeacherCourses(Teacher teacher) {
        List<String> courseIds = teacher.getCourseIds();

        // Clear the current list to avoid duplicates
        teacher.clearCoursesTaught();

        // For each courseId, get the actual course and add it to the teacher's coursesTaught
        for (String courseId : courseIds) {
            Course course = getCourse(courseId);
            if (course != null) {
                teacher.addCourseDirect(course);
                // Make sure the course has this teacher as instructor
                if (!course.getInstructor().equals(teacher)) {
                    course.setInstructor(teacher);
                }
            }
        }

        System.out.println("✓ Restored " + teacher.getCoursesTaught().size() +
                          " courses for teacher " + teacher.getName());
    }

    /**
     * Get courses by department
     */
    public List<Course> getCoursesByDepartment(String department) {
        return courses.values().stream()
                .filter(course -> course.getDepartment().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }

    /**
     * Get courses by semester
     */
    public List<Course> getCoursesBySemester(String semester) {
        return courses.values().stream()
                .filter(course -> course.getSemester().equalsIgnoreCase(semester))
                .collect(Collectors.toList());
    }

    /**
     * Get courses taught by teacher
     */
    public List<Course> getCoursesByTeacher(Teacher teacher) {
        return courses.values().stream()
                .filter(course -> course.getInstructor().equals(teacher))
                .collect(Collectors.toList());
    }

    /**
     * Get courses for student
     */
    public List<Course> getCoursesForStudent(Student student) {
        return courses.values().stream()
                .filter(course -> course.getEnrolledStudents().contains(student))
                .collect(Collectors.toList());
    }

    /**
     * Display course statistics
     */
    public void displayCourseStatistics(Course course) {
        System.out.println("=== Course Statistics ===");
        System.out.println("Course: " + course.getCourseName());
        System.out.println("Enrolled Students: " + course.getEnrolledStudents().size());
        System.out.println("Total Assignments: " + course.getAssignments().size());
        System.out.println("Instructor: " + course.getInstructor().getName());
    }

    // Getters
    public Course getCourse(String courseId) {
        return courses.get(courseId);
    }

    public List<Course> getAllCourses() {
        return new ArrayList<>(courses.values());
    }

    public Course findCourseByCode(String courseCode) {
        return courses.values().stream()
                .filter(course -> course.getCourseCode().equalsIgnoreCase(courseCode))
                .findFirst()
                .orElse(null);
    }
}