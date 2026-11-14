package com.examseating.algorithm;

import com.examseating.model.Classroom;
import com.examseating.model.Exam;
import com.examseating.model.SeatingArrangement;
import com.examseating.model.Student;

import java.util.*;

/**
 * Deterministic Rule-Based Seating Arrangement Algorithm
 * Compliant with specifications:
 * - Capacity checking
 * - Year-wise & Division grouping
 * - Alternating / rotating assignment across divisions
 * - Horizontal and vertical neighbour conflict checks
 * - Hold/Release check
 * - Detailed feedback / warning messages if constraints cannot be fully satisfied
 * - Strictly NO AI or Machine Learning
 */
public class RuleBasedSeatingGenerator {

    public static class SeatingResult {
        private boolean success;
        private String message;
        private int totalStudents;
        private int totalCapacity;
        private int totalAllocated;
        private int conflictCount;
        private List<String> warnings;
        private List<SeatingArrangement> allocations;

        public SeatingResult() {
            this.warnings = new ArrayList<>();
            this.allocations = new ArrayList<>();
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getTotalStudents() {
            return totalStudents;
        }

        public void setTotalStudents(int totalStudents) {
            this.totalStudents = totalStudents;
        }

        public int getTotalCapacity() {
            return totalCapacity;
        }

        public void setTotalCapacity(int totalCapacity) {
            this.totalCapacity = totalCapacity;
        }

        public int getTotalAllocated() {
            return totalAllocated;
        }

        public void setTotalAllocated(int totalAllocated) {
            this.totalAllocated = totalAllocated;
        }

        public int getConflictCount() {
            return conflictCount;
        }

        public void setConflictCount(int conflictCount) {
            this.conflictCount = conflictCount;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }

        public List<SeatingArrangement> getAllocations() {
            return allocations;
        }

        public void setAllocations(List<SeatingArrangement> allocations) {
            this.allocations = allocations;
        }
    }

    /**
     * Executes the Rule-Based Seating Algorithm
     */
    public SeatingResult generateSeating(Exam exam, List<Student> students, List<Classroom> classrooms) {
        SeatingResult result = new SeatingResult();

        if (exam == null) {
            result.setSuccess(false);
            result.setMessage("Error: No examination selected.");
            return result;
        }

        // Feature 4: Hold & Release check
        if (exam.isOnHold()) {
            result.setSuccess(false);
            result.setMessage("Cannot generate seating: The examination arrangement for '" + exam.getExamName()
                    + "' is currently ON HOLD (Locked). Please release the hold before regenerating.");
            return result;
        }

        if (students == null || students.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("Error: No students registered for " + exam.getAcademicYear() + ".");
            return result;
        }

        if (classrooms == null || classrooms.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("Error: No classrooms configured in the system.");
            return result;
        }

        int totalCapacity = 0;
        for (Classroom c : classrooms) {
            totalCapacity += c.getCapacity();
        }

        result.setTotalStudents(students.size());
        result.setTotalCapacity(totalCapacity);

        // Capacity check
        if (students.size() > totalCapacity) {
            result.setSuccess(false);
            result.setMessage("Capacity Error: Insufficient seating capacity! Total students ("
                    + students.size() + ") exceed total available seats (" + totalCapacity
                    + "). Please add more classrooms or benches.");
            return result;
        }

        // Group students by division
        Map<String, List<Student>> divisionMap = new LinkedHashMap<>();
        for (Student s : students) {
            String div = s.getDivisionName().trim().toUpperCase();
            divisionMap.computeIfAbsent(div, k -> new ArrayList<>()).add(s);
        }

        // Check if one division heavily dominates total students
        int maxInOneDiv = 0;
        String dominantDiv = "";
        for (Map.Entry<String, List<Student>> entry : divisionMap.entrySet()) {
            if (entry.getValue().size() > maxInOneDiv) {
                maxInOneDiv = entry.getValue().size();
                dominantDiv = entry.getKey();
            }
        }
        int otherStudents = students.size() - maxInOneDiv;
        if (divisionMap.size() > 1 && maxInOneDiv > (otherStudents + classrooms.size() * 2)) {
            result.getWarnings().add("Notice: Division '" + dominantDiv + "' has " + maxInOneDiv
                    + " students out of " + students.size()
                    + ". Due to division disproportion, some students from this division may inevitably sit next to each other on some benches.");
        } else if (divisionMap.size() == 1) {
            result.getWarnings().add("Notice: Only 1 division ('" + dominantDiv
                    + "') exists for this cohort. Neighbouring division diversity constraint cannot be applied.");
        }

        // Map to keep track of allocated seats in a 2D layout per classroom: roomNumber -> (bench -> (seat -> Student))
        Map<String, Map<Integer, Map<Integer, Student>>> grid = new HashMap<>();

        List<SeatingArrangement> allocations = new ArrayList<>();
        int conflictsDetected = 0;

        // Working queues for each division
        Map<String, Queue<Student>> queues = new HashMap<>();
        for (Map.Entry<String, List<Student>> e : divisionMap.entrySet()) {
            queues.put(e.getKey(), new LinkedList<>(e.getValue()));
        }

        int remainingStudents = students.size();

        // Iterate through classrooms, benches, and seats
        for (Classroom room : classrooms) {
            if (remainingStudents == 0) break;

            String roomNum = room.getRoomNumber();
            grid.putIfAbsent(roomNum, new HashMap<>());

            for (int b = 1; b <= room.getBenches(); b++) {
                if (remainingStudents == 0) break;
                grid.get(roomNum).putIfAbsent(b, new HashMap<>());

                for (int s = 1; s <= room.getSeatsPerBench(); s++) {
                    if (remainingStudents == 0) break;

                    // Inspect neighbours
                    // 1. Horizontal neighbour (immediate left seat on same bench)
                    Student leftStudent = grid.get(roomNum).get(b).get(s - 1);
                    String leftDiv = (leftStudent != null) ? leftStudent.getDivisionName() : null;

                    // 2. Vertical neighbour (same seat on the bench directly in front)
                    Student frontStudent = (b > 1 && grid.get(roomNum).containsKey(b - 1)) ?
                            grid.get(roomNum).get(b - 1).get(s) : null;
                    String frontDiv = (frontStudent != null) ? frontStudent.getDivisionName() : null;

                    // Select the best division to allocate:
                    // Priority 1: No horizontal conflict AND no vertical conflict, with largest remaining count
                    // Priority 2: No horizontal conflict, with largest remaining count
                    // Priority 3: Fallback (any division with students remaining, logged as conflict)

                    String selectedDiv = null;
                    int conflictFlag = 0;
                    String conflictReason = "";

                    // Candidate list sorted by remaining student count descending
                    List<String> activeDivs = new ArrayList<>();
                    for (Map.Entry<String, Queue<Student>> entry : queues.entrySet()) {
                        if (!entry.getValue().isEmpty()) {
                            activeDivs.add(entry.getKey());
                        }
                    }
                    activeDivs.sort((d1, d2) -> Integer.compare(queues.get(d2).size(), queues.get(d1).size()));

                    // Try Priority 1 (Clean: avoids both horizontal & vertical)
                    for (String div : activeDivs) {
                        if (!div.equalsIgnoreCase(leftDiv) && !div.equalsIgnoreCase(frontDiv)) {
                            selectedDiv = div;
                            break;
                        }
                    }

                    // Try Priority 2 (Avoids horizontal conflict)
                    if (selectedDiv == null) {
                        for (String div : activeDivs) {
                            if (!div.equalsIgnoreCase(leftDiv)) {
                                selectedDiv = div;
                                break;
                            }
                        }
                    }

                    // Fallback (Priority 3: all active divisions conflict with neighbour)
                    if (selectedDiv == null && !activeDivs.isEmpty()) {
                        selectedDiv = activeDivs.get(0);
                        conflictFlag = 1;
                        conflictReason = "Same division (" + selectedDiv + ") seated beside or adjacent due to student distribution.";
                        conflictsDetected++;
                    }

                    if (selectedDiv != null) {
                        Student chosen = queues.get(selectedDiv).poll();
                        grid.get(roomNum).get(b).put(s, chosen);
                        remainingStudents--;

                        SeatingArrangement arr = new SeatingArrangement(
                                exam.getId(),
                                chosen.getId(),
                                room.getId(),
                                room.getRoomNumber(),
                                b,
                                s,
                                chosen.getName(),
                                chosen.getRollNo(),
                                chosen.getDivisionName(),
                                chosen.getAcademicYear()
                        );
                        arr.setConflictFlag(conflictFlag);
                        arr.setConflictReason(conflictReason);
                        arr.setExamName(exam.getExamName());
                        arr.setExamSubject(exam.getSubject());
                        arr.setExamDate(exam.getExamDate());
                        arr.setStartTime(exam.getStartTime());
                        arr.setEndTime(exam.getEndTime());
                        arr.setExamTime(exam.getExamTime());

                        allocations.add(arr);
                    }
                }
            }
        }

        result.setAllocations(allocations);
        result.setTotalAllocated(allocations.size());
        result.setConflictCount(conflictsDetected);

        if (conflictsDetected > 0) {
            result.getWarnings().add("Seating rule constraint note: " + conflictsDetected
                    + " seat(s) could not satisfy strict division separation due to division imbalance.");
        }

        result.setSuccess(true);
        result.setMessage("Seating arrangement successfully generated for " + allocations.size()
                + " students (" + exam.getAcademicYear() + ") across " + classrooms.size() + " classrooms (" + conflictsDetected + " constraint warnings).");

        return result;
    }
}
