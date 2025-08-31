package bg.sofia.uni.fmi.webjava.backend.utils;

import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CoursePreviewDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.request.EnrollmentRequestResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Assignment;
import bg.sofia.uni.fmi.webjava.backend.model.entity.AssignmentSubmission;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Enrollment;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentRequest;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentState;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Section;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.model.entity.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public class TestUtils {

    private static final UUID STANDARD_USER_ID = UUID.randomUUID();
    private static final String STANDARD_USER_USERNAME = "testUser";
    private static final String STANDARD_USER_EMAIL = "test@example.com";
    private static final String STANDARD_USER_PASSWORD = "Testpass123";
    private static final String STANDARD_USER_FIRST_NAME = "Test";

    private static final UUID ADMIN_USER_ID = UUID.randomUUID();
    private static final String ADMIN_USER_USERNAME = "adminUser";
    private static final String ADMIN_USER_EMAIL = "admin@example.com";
    private static final String ADMIN_USER_PASSWORD = "Adminpass123";
    private static final String ADMIN_USER_FIRST_NAME = "Admin";
    private static final String USER_LAST_NAME = "User";

    private static final UUID COURSE_ID = UUID.randomUUID();
    private static final String COURSE_TITLE = "Test Course";
    private static final String COURSE_DESCRIPTION = "This is a test course description.";
    private static final String COURSE_IMAGE_URL = "https://example.com/image.png";

    private static final String SECTION_NAME = "Test Section";
    private static final String SECTION_TEXT_CONTENT = "This is a test section description.";

    private static final String ASSIGNMENT_TITLE = "Test Assignment";
    private static final String ASSIGNMENT_DESCRIPTION = "This is a test assignment description.";
    private static final LocalDateTime ASSIGNMENT_DEADLINE = LocalDateTime.now();
    private static final double ASSIGNMENT_TOTAL_POINTS = 10.0;

    private static final String SUBMISSION_URL = "Test Submission";
    private static final String SUBMISSION_COMMENT = "This is a test submission comment.";
    private static final String SUBMISSION_FEEDBACK = "Good job!";
    private static final double SUBMISSION_GRADE = 10.0;

    private static final UUID ENROLLMENT_ID = UUID.randomUUID();
    private static final UUID ENROLLMENT_REQUEST_ID = UUID.randomUUID();
    private static final UUID SECTION_ID = UUID.randomUUID();
    private static final UUID ASSIGNMENT_ID = UUID.randomUUID();
    private static final UUID SUBMISSION_ID = UUID.randomUUID();

    public static User createStandardTestUser() {
        User user = new User();
        user.setId(STANDARD_USER_ID);
        user.setFirstName(STANDARD_USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setUsername(STANDARD_USER_USERNAME);
        user.setEmail(STANDARD_USER_EMAIL);
        user.setPassword(STANDARD_USER_PASSWORD);
        user.setRole(UserRole.STANDARD);
        return user;
    }

    public static User createAdminTestUser() {
        User admin = new User();
        admin.setId(ADMIN_USER_ID);
        admin.setFirstName(ADMIN_USER_FIRST_NAME);
        admin.setLastName(USER_LAST_NAME);
        admin.setUsername(ADMIN_USER_USERNAME);
        admin.setEmail(ADMIN_USER_EMAIL);
        admin.setPassword(ADMIN_USER_PASSWORD);
        admin.setRole(UserRole.ADMIN);
        return admin;
    }

    public static UserResponseDto createStandardUserResponseDto() {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(STANDARD_USER_ID);
        userResponseDto.setFirstName(STANDARD_USER_FIRST_NAME);
        userResponseDto.setLastName(USER_LAST_NAME);
        userResponseDto.setUsername(STANDARD_USER_USERNAME);
        userResponseDto.setEmail(STANDARD_USER_EMAIL);
        return userResponseDto;
    }

    public static UserResponseDto createAdminUserResponseDto() {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(ADMIN_USER_ID);
        userResponseDto.setFirstName(ADMIN_USER_FIRST_NAME);
        userResponseDto.setLastName(USER_LAST_NAME);
        userResponseDto.setUsername(ADMIN_USER_USERNAME);
        userResponseDto.setEmail(ADMIN_USER_EMAIL);
        return userResponseDto;
    }

    public static Course createTestCourse() {
        Course course = new Course();
        course.setId(COURSE_ID);
        course.setTitle(COURSE_TITLE);
        course.setDescription(COURSE_DESCRIPTION);
        course.setImageUrl(COURSE_IMAGE_URL);
        return course;
    }

    public static CourseResponseDto createTestCourseResponseDto() {
        CourseResponseDto courseResponseDto = new CourseResponseDto();
        courseResponseDto.setId(COURSE_ID);
        courseResponseDto.setTitle(COURSE_TITLE);
        courseResponseDto.setDescription(COURSE_DESCRIPTION);
        courseResponseDto.setImageUrl(COURSE_IMAGE_URL);
        return courseResponseDto;
    }

    public static CoursePreviewDto createTestCoursePreviewDto() {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setId(COURSE_ID);
        coursePreviewDto.setTitle(COURSE_TITLE);
        coursePreviewDto.setImageUrl(COURSE_IMAGE_URL);
        return coursePreviewDto;
    }

    public static Enrollment createTestEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(ENROLLMENT_ID);
        enrollment.setUser(createStandardTestUser());
        enrollment.setCourse(createTestCourse());
        return enrollment;
    }

    public static EnrollmentResponseDto createTestEnrollmentResponseDto() {
        EnrollmentResponseDto enrollmentResponseDto = new EnrollmentResponseDto();
        enrollmentResponseDto.setId(ENROLLMENT_ID);
        enrollmentResponseDto.setUser(createStandardUserResponseDto());
        enrollmentResponseDto.setCourse(createTestCoursePreviewDto());
        return enrollmentResponseDto;
    }

    public static EnrollmentRequest createTestEnrollmentRequest() {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setId(ENROLLMENT_REQUEST_ID);
        enrollmentRequest.setUser(createStandardTestUser());
        enrollmentRequest.setCourse(createTestCourse());
        enrollmentRequest.setState(EnrollmentState.PENDING);
        return enrollmentRequest;
    }

    public static EnrollmentRequestResponseDto createTestEnrollmentRequestResponseDto() {
        EnrollmentRequestResponseDto enrollmentRequestResponseDto = new EnrollmentRequestResponseDto();
        enrollmentRequestResponseDto.setId(ENROLLMENT_REQUEST_ID);
        enrollmentRequestResponseDto.setUser(createStandardUserResponseDto());
        enrollmentRequestResponseDto.setCourse(createTestCoursePreviewDto());
        enrollmentRequestResponseDto.setState(EnrollmentState.PENDING);
        return enrollmentRequestResponseDto;
    }

    public static Section createTestSection() {
        Section section = new Section();
        section.setId(SECTION_ID);
        section.setTitle("Test Section");
        section.setTextContent("This is a test section description.");
        section.setOrderNumber(1);
        section.setCourse(createTestCourse());
        return section;
    }

    public static SectionResponseDto createTestSectionResponseDto() {
        SectionResponseDto sectionResponseDto = new SectionResponseDto();
        sectionResponseDto.setId(SECTION_ID);
        sectionResponseDto.setTitle(SECTION_NAME);
        sectionResponseDto.setTextContent(SECTION_TEXT_CONTENT);
        sectionResponseDto.setOrderNumber(1);
        return sectionResponseDto;
    }

    public static Assignment createTestAssignment() {
        Assignment assignment = new Assignment();
        assignment.setId(ASSIGNMENT_ID);
        assignment.setTitle(ASSIGNMENT_TITLE);
        assignment.setDescription(ASSIGNMENT_DESCRIPTION);
        assignment.setDeadline(ASSIGNMENT_DEADLINE);
        assignment.setTotalPoints(ASSIGNMENT_TOTAL_POINTS);
        assignment.setCourse(createTestCourse());
        return assignment;
    }

    public static AssignmentResponseDto createTestAssignmentResponseDto() {
        AssignmentResponseDto assignmentResponseDto = new AssignmentResponseDto();
        assignmentResponseDto.setId(ASSIGNMENT_ID);
        assignmentResponseDto.setTitle(ASSIGNMENT_TITLE);
        assignmentResponseDto.setDescription(ASSIGNMENT_DESCRIPTION);
        assignmentResponseDto.setDeadline(ASSIGNMENT_DEADLINE);
        assignmentResponseDto.setTotalPoints(ASSIGNMENT_TOTAL_POINTS);
        assignmentResponseDto.setCourse(createTestCoursePreviewDto());
        return assignmentResponseDto;
    }

    public static AssignmentSubmission createTestAssignmentSubmission() {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setId(SUBMISSION_ID);
        submission.setSubmissionUrl(SUBMISSION_URL);
        submission.setComment(SUBMISSION_COMMENT);
        submission.setFeedback(SUBMISSION_FEEDBACK);
        submission.setGrade(SUBMISSION_GRADE);
        submission.setAssignment(createTestAssignment());
        submission.setUser(createStandardTestUser());
        return submission;
    }

    public static AssignmentSubmissionResponseDto createTestAssignmentSubmissionResponseDto() {
        AssignmentSubmissionResponseDto submissionResponseDto = new AssignmentSubmissionResponseDto();
        submissionResponseDto.setId(SUBMISSION_ID);
        submissionResponseDto.setSubmissionUrl(SUBMISSION_URL);
        submissionResponseDto.setComment(SUBMISSION_COMMENT);
        submissionResponseDto.setFeedback(SUBMISSION_FEEDBACK);
        submissionResponseDto.setGrade(SUBMISSION_GRADE);
        submissionResponseDto.setAssignment(createTestAssignmentResponseDto());
        submissionResponseDto.setUser(createStandardUserResponseDto());
        return submissionResponseDto;
    }

}
