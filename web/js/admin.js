/**
 * Admin Dashboard Controller - ExamSeat Pro
 */

let dashboardData = {};
let currentAllocations = [];
let allClassrooms = [];
let allDivisions = [];
let allStudents = [];
let allExams = [];
let currentYearFilter = 'All Years';
let currentViewMode = 'grid';

document.addEventListener('DOMContentLoaded', async () => {
    const user = getUser();
    if (!user || user.role !== 'ADMIN') {
        window.location.href = 'index.html';
        return;
    }

    document.getElementById('admin-name-badge').innerText = user.name || user.username;
    await loadInitialData();
});

// Navigation Switcher
function showSection(sectionId) {
    document.querySelectorAll('.content-section').forEach(sec => sec.style.display = 'none');
    document.querySelectorAll('.sidebar-menu .menu-item').forEach(item => item.classList.remove('active'));

    const targetSec = document.getElementById(`section-${sectionId}`);
    if (targetSec) targetSec.style.display = 'block';

    document.querySelectorAll('.sidebar-menu .menu-item').forEach(item => {
        if (item.getAttribute('onclick') && item.getAttribute('onclick').includes(sectionId)) {
            item.classList.add('active');
        }
    });

    if (sectionId === 'overview') loadOverviewStats();
    if (sectionId === 'students') loadStudents();
    if (sectionId === 'timetable') loadTimetable();
    if (sectionId === 'divisions') loadDivisions();
    if (sectionId === 'classrooms') loadClassrooms();
    if (sectionId === 'generate') prepareGenerationView();
    if (sectionId === 'seating-view') loadSeatingArrangement();
}

async function loadInitialData() {
    await checkDatabaseStatus();
    await loadOverviewStats();
    await loadDivisions();
    await loadClassrooms();
    await loadTimetable();
}

async function checkDatabaseStatus() {
    try {
        const res = await fetchApi('/database/status');
        const badge = document.getElementById('admin-db-badge');
        badge.innerText = `DB: ${res.type.toUpperCase()}`;
        badge.className = (res.type === 'mysql') ? 'badge badge-success' : 'badge badge-info';
        badge.title = res.info;
    } catch (e) {
        console.error(e);
    }
}

// 1. OVERVIEW STATISTICS & METRICS
async function loadOverviewStats() {
    try {
        const stats = await fetchApi('/dashboard/stats');
        dashboardData = stats;

        document.getElementById('stat-students').innerText = stats.totalStudents;
        document.getElementById('stat-capacity').innerText = stats.totalCapacity;
        document.getElementById('stat-classrooms').innerText = stats.totalClassrooms;
        document.getElementById('stat-exams').innerText = stats.totalExams || 0;

        // Year Distribution counts
        if (stats.yearDistribution) {
            document.getElementById('year-count-fe').innerText = stats.yearDistribution['First Year'] || 0;
            document.getElementById('year-count-se').innerText = stats.yearDistribution['Second Year'] || 0;
            document.getElementById('year-count-te').innerText = stats.yearDistribution['Third Year'] || 0;
            document.getElementById('year-count-be').innerText = stats.yearDistribution['Fourth Year'] || 0;
        }

        const examBadge = document.getElementById('active-exam-badge');
        examBadge.innerText = `Exam: ${stats.activeExamSubject}`;

        const holdBadge = document.getElementById('active-hold-badge');
        const overviewHoldBadge = document.getElementById('overview-hold-status-badge');
        const overviewHoldText = document.getElementById('overview-hold-text');

        if (stats.activeExamOnHold) {
            holdBadge.className = 'badge badge-hold';
            holdBadge.innerText = '🔒 ON HOLD';
            overviewHoldBadge.className = 'badge badge-hold';
            overviewHoldBadge.innerText = '🔒 ON HOLD';
            overviewHoldText.innerHTML = '<span style="color: var(--danger); font-weight: 700;">Locked / On Hold (Regeneration Prevented)</span>';
        } else {
            holdBadge.className = 'badge badge-released';
            holdBadge.innerText = '🔓 RELEASED';
            overviewHoldBadge.className = 'badge badge-released';
            overviewHoldBadge.innerText = '🔓 RELEASED';
            overviewHoldText.innerHTML = '<span style="color: var(--success); font-weight: 600;">Released (Ready for Generation)</span>';
        }

        document.getElementById('overview-exam-name').innerText = stats.activeExamName || '-';
        document.getElementById('overview-exam-subject').innerText = stats.activeExamSubject || '-';
        document.getElementById('overview-exam-year').innerText = stats.activeExamYear || 'All Years';
        document.getElementById('overview-exam-datetime').innerText = `${stats.activeExamDate} (${stats.activeExamTime})`;

        const allocBadge = document.getElementById('allocation-status-badge');
        if (stats.seatingGenerated) {
            allocBadge.innerText = 'Arrangement Generated';
            allocBadge.className = 'badge badge-success';
        } else {
            allocBadge.innerText = 'Not Generated';
            allocBadge.className = 'badge badge-warning';
        }

        document.getElementById('overview-allocated-count').innerText = `${stats.totalAllocated} Students Allocated`;

        const capStatus = document.getElementById('overview-capacity-status');
        if (stats.totalCapacity >= stats.totalStudents) {
            capStatus.innerHTML = `<span style="color: var(--success); font-weight: 600;">Sufficient Total Capacity</span> (${stats.totalStudents} students &le; ${stats.totalCapacity} seats)`;
        } else {
            capStatus.innerHTML = `<span style="color: var(--danger); font-weight: 600;">Capacity Deficit</span> (Need ${stats.totalStudents - stats.totalCapacity} more seats!)`;
        }

    } catch (e) {
        showToast(e.message, 'error');
    }
}

// 2. YEAR-WISE STUDENTS MANAGEMENT & EXCEL
async function loadStudents() {
    try {
        allStudents = await fetchApi('/students');
        renderStudentsTable();
        populateDivisionDropdown();
    } catch (e) {
        showToast('Error loading students: ' + e.message, 'error');
    }
}

function filterByYear(year, element) {
    currentYearFilter = year;
    document.querySelectorAll('.year-pill-tabs .year-pill').forEach(p => p.classList.remove('active'));
    if (element) element.classList.add('active');
    // Update delete-all button label to reflect current filter
    const lbl = document.getElementById('delete-all-btn-label');
    if (lbl) {
        lbl.textContent = (year === 'All Years') ? 'Delete All Students' : `Delete All ${year} Students`;
    }
    renderStudentsTable();
}

function renderStudentsTable() {
    const tbody = document.getElementById('students-table-body');
    tbody.innerHTML = '';

    const q = (document.getElementById('student-search-input').value || '').toLowerCase();
    
    let filtered = allStudents;
    if (currentYearFilter !== 'All Years') {
        filtered = filtered.filter(s => (s.academicYear || 'First Year') === currentYearFilter);
    }

    if (q) {
        filtered = filtered.filter(s =>
            (s.rollNo && s.rollNo.toLowerCase().includes(q)) ||
            (s.name && s.name.toLowerCase().includes(q)) ||
            (s.academicYear && s.academicYear.toLowerCase().includes(q)) ||
            (s.divisionName && s.divisionName.toLowerCase().includes(q))
        );
    }

    if (!filtered || filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No students found for this selection.</td></tr>';
        return;
    }

    filtered.forEach((s, idx) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${idx + 1}</td>
            <td>${getYearBadgeHtml(s.academicYear)}</td>
            <td><strong>${s.rollNo}</strong></td>
            <td>${s.name}</td>
            <td>${getDivisionBadgeHtml(s.divisionName)}</td>
            <td><code>${s.username}</code></td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="deleteStudent(${s.id})">Delete</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function filterStudentsTable() {
    renderStudentsTable();
}

async function handleAddStudent(e) {
    e.preventDefault();
    const academicYear = document.getElementById('std-year').value;
    const rollNo = document.getElementById('std-roll').value.trim();
    const name = document.getElementById('std-name').value.trim();
    const divisionName = document.getElementById('std-division').value;
    const username = document.getElementById('std-username').value.trim();
    const password = document.getElementById('std-password').value.trim();

    try {
        const res = await fetchApi('/students', 'POST', {
            academicYear,
            rollNo,
            name,
            divisionName,
            username,
            password
        });
        showToast(res.message, 'success');
        document.getElementById('form-add-student').reset();
        await loadStudents();
        await loadOverviewStats();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function deleteStudent(id) {
    if (!confirm('Are you sure you want to delete this student record?')) return;
    try {
        const res = await fetchApi('/students/delete', 'POST', { id: String(id) });
        showToast(res.message, 'success');
        await loadStudents();
        await loadOverviewStats();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

// EXCEL IMPORT MODAL LOGIC (Feature 2)
function openExcelImportModal() {
    document.getElementById('excel-import-modal').style.display = 'flex';
}

function closeExcelImportModal() {
    document.getElementById('excel-import-modal').style.display = 'none';
}

function handleExcelFileUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        document.getElementById('excel-paste-text').value = e.target.result;
        showToast(`Loaded ${file.name} successfully! Click "Import Students Now".`, 'info');
    };
    reader.readAsText(file);
}

async function submitExcelImport() {
    const textData = document.getElementById('excel-paste-text').value.trim();
    const defaultYear = document.getElementById('excel-default-year').value;

    if (!textData) {
        showToast('Please select a file or paste Excel/CSV data.', 'warning');
        return;
    }

    try {
        const res = await fetchApi('/excel/import', 'POST', {
            excelData: textData,
            defaultYear
        });
        showToast(res.message, res.success ? 'success' : 'error');
        closeExcelImportModal();
        document.getElementById('excel-paste-text').value = '';
        await loadStudents();
        await loadOverviewStats();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

// 3. EXAM TIMETABLE MANAGEMENT & HOLD/RELEASE (Features 3 & 4)
async function loadTimetable() {
    try {
        allExams = await fetchApi('/exams');
        const tbody = document.getElementById('timetable-table-body');
        tbody.innerHTML = '';
        document.getElementById('tt-count-badge').innerText = `${allExams.length} Exams`;

        populateExamSelectDropdowns();

        if (!allExams || allExams.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No exams in timetable. Add one above.</td></tr>';
            return;
        }

        allExams.forEach(ex => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${ex.examName}</strong></td>
                <td>${ex.subject}</td>
                <td>${getYearBadgeHtml(ex.academicYear)}</td>
                <td>${ex.examDate}</td>
                <td><code>${ex.examTime}</code></td>
                <td>${getHoldBadgeHtml(ex.onHold)}</td>
                <td style="white-space: nowrap;">
                    ${ex.onHold ? 
                        `<button class="btn btn-release btn-sm" onclick="setExamHold(${ex.id}, false)">🔓 Release</button>` : 
                        `<button class="btn btn-hold btn-sm" onclick="setExamHold(${ex.id}, true)">🔒 Hold</button>`
                    }
                    <button class="btn btn-primary btn-sm" onclick="selectAndGenerateExam(${ex.id})">⚙️ Generate</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteExam(${ex.id})">Delete</button>
                </td>
            `;
            tbody.appendChild(tr);
        });

    } catch (e) {
        showToast('Error loading timetable: ' + e.message, 'error');
    }
}

function populateExamSelectDropdowns() {
    const genSelect = document.getElementById('gen-exam-select');
    const viewSelect = document.getElementById('view-exam-select');

    if (genSelect) genSelect.innerHTML = '';
    if (viewSelect) viewSelect.innerHTML = '';

    allExams.forEach(ex => {
        const optText = `${ex.examName} - ${ex.subject} (${ex.academicYear}) [${ex.examDate}] ${ex.onHold ? '🔒' : ''}`;
        
        if (genSelect) {
            const opt = document.createElement('option');
            opt.value = ex.id;
            opt.innerText = optText;
            genSelect.appendChild(opt);
        }

        if (viewSelect) {
            const opt = document.createElement('option');
            opt.value = ex.id;
            opt.innerText = optText;
            viewSelect.appendChild(opt);
        }
    });

    onGenExamSelectChange();
}

async function handleAddTimetableExam(e) {
    e.preventDefault();
    const examName = document.getElementById('tt-exam-name').value.trim();
    const subject = document.getElementById('tt-subject').value.trim();
    const academicYear = document.getElementById('tt-year').value;
    const examDate = document.getElementById('tt-date').value.trim();
    const startTime = document.getElementById('tt-start-time').value.trim();
    const endTime = document.getElementById('tt-end-time').value.trim();

    try {
        const res = await fetchApi('/exams', 'POST', {
            examName,
            subject,
            academicYear,
            examDate,
            startTime,
            endTime
        });
        showToast(res.message, 'success');
        document.getElementById('tt-exam-name').value = '';
        document.getElementById('tt-subject').value = '';
        await loadTimetable();
        await loadOverviewStats();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function setExamHold(examId, hold) {
    try {
        const res = await fetchApi('/exams/hold', 'POST', {
            examId: String(examId),
            hold: String(hold)
        });
        showToast(res.message, hold ? 'warning' : 'success');
        await loadTimetable();
        await loadOverviewStats();
        onGenExamSelectChange();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function deleteExam(id) {
    if (!confirm('Are you sure you want to delete this exam schedule?')) return;
    try {
        const res = await fetchApi('/exams/delete', 'POST', { id: String(id) });
        showToast(res.message, 'success');
        await loadTimetable();
        await loadOverviewStats();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function selectAndGenerateExam(examId) {
    showSection('generate');
    const genSelect = document.getElementById('gen-exam-select');
    if (genSelect) {
        genSelect.value = examId;
        onGenExamSelectChange();
    }
}

// 4. DIVISIONS MANAGEMENT
async function loadDivisions() {
    try {
        allDivisions = await fetchApi('/divisions');
        const tbody = document.getElementById('divisions-table-body');
        tbody.innerHTML = '';

        if (!allDivisions || allDivisions.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">No divisions found.</td></tr>';
            return;
        }

        allDivisions.forEach(d => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${getDivisionBadgeHtml(d.divisionName)}</td>
                <td>${d.description || '-'}</td>
                <td><strong>${d.studentCount || 0}</strong> students</td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="deleteDivision('${d.divisionName}')">Delete</button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        populateDivisionDropdown();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function populateDivisionDropdown() {
    const sel = document.getElementById('std-division');
    if (!sel) return;
    sel.innerHTML = '';
    allDivisions.forEach(d => {
        const opt = document.createElement('option');
        opt.value = d.divisionName;
        opt.innerText = `Division ${d.divisionName} (${d.description || ''})`;
        sel.appendChild(opt);
    });
}

async function handleAddDivision(e) {
    e.preventDefault();
    const divisionName = document.getElementById('div-name').value.trim().toUpperCase();
    const description = document.getElementById('div-desc').value.trim();

    try {
        const res = await fetchApi('/divisions', 'POST', { divisionName, description });
        showToast(res.message, 'success');
        document.getElementById('div-name').value = '';
        document.getElementById('div-desc').value = '';
        await loadDivisions();
        await loadOverviewStats();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function deleteDivision(name) {
    if (!confirm(`Are you sure you want to delete Division ${name}?`)) return;
    try {
        const res = await fetchApi('/divisions/delete', 'POST', { divisionName: name });
        showToast(res.message, 'success');
        await loadDivisions();
        await loadOverviewStats();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

// 5. CLASSROOMS & BENCHES
async function loadClassrooms() {
    try {
        allClassrooms = await fetchApi('/classrooms');
        const tbody = document.getElementById('classrooms-table-body');
        tbody.innerHTML = '';

        if (!allClassrooms || allClassrooms.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">No classrooms added yet.</td></tr>';
            return;
        }

        allClassrooms.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${c.roomNumber}</strong></td>
                <td>${c.benches} benches</td>
                <td>${c.seatsPerBench} seats/bench</td>
                <td><span class="badge badge-info">${c.capacity} seats</span></td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="deleteClassroom(${c.id})">Delete</button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Filter classroom select
        const filterSel = document.getElementById('filter-room');
        if (filterSel) {
            filterSel.innerHTML = '<option value="ALL">All Classrooms</option>';
            allClassrooms.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.roomNumber;
                opt.innerText = `${c.roomNumber} (${c.capacity} seats)`;
                filterSel.appendChild(opt);
            });
        }

    } catch (e) {
        showToast(e.message, 'error');
    }
}

function updateRoomCalc() {
    const b = parseInt(document.getElementById('room-benches').value) || 0;
    const s = parseInt(document.getElementById('room-seats').value) || 0;
    document.getElementById('calculated-room-cap').innerText = b * s;
}

async function handleAddClassroom(e) {
    e.preventDefault();
    const roomNumber = document.getElementById('room-name').value.trim();
    const benches = document.getElementById('room-benches').value;
    const seatsPerBench = document.getElementById('room-seats').value;

    try {
        const res = await fetchApi('/classrooms', 'POST', {
            roomNumber,
            benches,
            seatsPerBench
        });
        showToast(res.message, 'success');
        document.getElementById('room-name').value = '';
        await loadClassrooms();
        await loadOverviewStats();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function deleteClassroom(id) {
    if (!confirm('Are you sure you want to delete this classroom?')) return;
    try {
        const res = await fetchApi('/classrooms/delete', 'POST', { id: String(id) });
        showToast(res.message, 'success');
        await loadClassrooms();
        await loadOverviewStats();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

// 6. GENERATE SEATING ARRANGEMENT & HOLD CHECK (Features 4 & 5)
function onGenExamSelectChange() {
    const genSelect = document.getElementById('gen-exam-select');
    if (!genSelect || !genSelect.value) return;

    const examId = parseInt(genSelect.value);
    const exam = allExams.find(x => x.id === examId);
    if (!exam) return;

    const badgeDiv = document.getElementById('gen-exam-hold-badge');
    const lockWarn = document.getElementById('hold-lock-warning');
    const runBtn = document.getElementById('btn-run-algorithm');
    const toggleHoldBtn = document.getElementById('btn-toggle-hold');

    if (exam.onHold) {
        badgeDiv.innerHTML = '<span class="badge badge-hold">🔒 ARRANGEMENT ON HOLD (LOCKED)</span>';
        lockWarn.style.display = 'block';
        runBtn.disabled = true;
        runBtn.style.opacity = '0.5';
        runBtn.title = 'Arrangement is locked. Release hold to regenerate.';
        toggleHoldBtn.innerText = '🔓 Release Hold';
        toggleHoldBtn.className = 'btn btn-release';
    } else {
        badgeDiv.innerHTML = '<span class="badge badge-released">🔓 READY FOR GENERATION</span>';
        lockWarn.style.display = 'none';
        runBtn.disabled = false;
        runBtn.style.opacity = '1';
        runBtn.title = '';
        toggleHoldBtn.innerText = '🔒 Put on Hold';
        toggleHoldBtn.className = 'btn btn-secondary';
    }

    // Update capacity progress for this specific year
    let targetStudents = allStudents;
    if (exam.academicYear && exam.academicYear !== 'All Years') {
        targetStudents = allStudents.filter(s => (s.academicYear || 'First Year') === exam.academicYear);
    }
    const cohortCount = targetStudents.length;
    const capacity = dashboardData.totalCapacity || 0;

    const ratioSpan = document.getElementById('capacity-check-ratio');
    ratioSpan.innerText = `${cohortCount} ${exam.academicYear} Students / ${capacity} Available Seats`;

    const fill = document.getElementById('capacity-progress-fill');
    const warn = document.getElementById('capacity-warning-text');

    if (capacity === 0) {
        fill.style.width = '0%';
        fill.className = 'progress-bar-fill danger';
        warn.innerHTML = `<span style="color: var(--danger); font-weight: 600;">❌ No classrooms added yet. Add classrooms before generating.</span>`;
        return;
    }

    const pct = Math.min(100, Math.round((cohortCount / capacity) * 100));
    fill.style.width = pct + '%';

    if (cohortCount > capacity) {
        fill.className = 'progress-bar-fill danger';
        warn.innerHTML = `<span style="color: var(--danger); font-weight: 600;">⚠️ Capacity Shortage! Need ${cohortCount - capacity} more seats before generating.</span>`;
    } else {
        fill.className = 'progress-bar-fill';
        warn.innerHTML = `<span style="color: var(--success); font-weight: 600;">✅ Capacity Sufficient (${pct}% room utilization for ${exam.academicYear}).</span>`;
    }
}

async function toggleHoldForSelectedExam() {
    const genSelect = document.getElementById('gen-exam-select');
    if (!genSelect || !genSelect.value) return;

    const examId = parseInt(genSelect.value);
    const exam = allExams.find(x => x.id === examId);
    if (!exam) return;

    await setExamHold(examId, !exam.onHold);
}

async function releaseCurrentHold() {
    const genSelect = document.getElementById('gen-exam-select');
    if (!genSelect || !genSelect.value) return;
    await setExamHold(parseInt(genSelect.value), false);
}

function prepareGenerationView() {
    populateExamSelectDropdowns();
}

async function triggerGenerateSeating() {
    const audit = document.getElementById('audit-log');
    const genSelect = document.getElementById('gen-exam-select');
    const examId = genSelect ? parseInt(genSelect.value) : (dashboardData.activeExamId || 0);
    const exam = allExams.find(x => x.id === examId);

    if (exam && exam.onHold) {
        showToast('Arrangement is ON HOLD. Please release hold first.', 'warning');
        return;
    }

    audit.innerHTML = `<div>[Step 1] Initializing ExamSeat Pro Rule-Based Algorithm for Exam ID: ${examId}...</div>`;

    const btn = document.getElementById('btn-run-algorithm');
    btn.disabled = true;
    btn.innerText = 'Calculating Allocation...';

    try {
        audit.innerHTML += `<div>[Step 2] Selected Exam: ${exam ? exam.examName : 'Active Exam'} (${exam ? exam.academicYear : 'All Years'})...</div>`;
        audit.innerHTML += `<div>[Step 3] Partitioning student cohort into division queues...</div>`;
        audit.innerHTML += `<div>[Step 4] Checking 2D classroom benches matrix and neighbouring constraints...</div>`;

        const res = await fetchApi('/seating/generate', 'POST', { examId: String(examId) });

        if (res.success) {
            audit.innerHTML += `<div class="success-line">[Step 5] SUCCESS: ${res.allocations.length} students allocated across classrooms!</div>`;
            audit.innerHTML += `<div class="success-line">[Step 6] Division constraint checks passed with ${res.conflictCount} warning(s).</div>`;
            
            if (res.warnings && res.warnings.length > 0) {
                res.warnings.forEach(w => {
                    audit.innerHTML += `<div class="warn-line">[Constraint Note] ${w}</div>`;
                });
            }

            audit.innerHTML += `<div class="success-line">[Step 7] Arrangement persisted to MySQL/database via JDBC transaction.</div>`;
            showToast('Seating arrangement generated successfully!', 'success');
            await loadOverviewStats();
        } else {
            audit.innerHTML += `<div class="err-line">[Error] ${res.message}</div>`;
            showToast(res.message, 'error');
        }
    } catch (e) {
        audit.innerHTML += `<div class="err-line">[Error] ${e.message}</div>`;
        showToast(e.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerText = '🚀 Generate Seating Arrangement Now';
        audit.scrollTop = audit.scrollHeight;
    }
}

// 7. VIEW SEATING ARRANGEMENT & EXCEL EXPORT (Feature 2)
async function loadSeatingArrangement() {
    try {
        const viewSelect = document.getElementById('view-exam-select');
        let examId = viewSelect && viewSelect.value ? parseInt(viewSelect.value) : (dashboardData.activeExamId || 0);

        currentAllocations = await fetchApi(`/seating/exam?examId=${examId}`);
        renderSeatingMatrix();
    } catch (e) {
        showToast('Error loading seating: ' + e.message, 'error');
    }
}

function onViewExamChange() {
    loadSeatingArrangement();
}

function toggleViewMode(mode) {
    currentViewMode = mode;
    document.getElementById('seating-grid-container').style.display = (mode === 'grid') ? 'block' : 'none';
    document.getElementById('seating-table-container').style.display = (mode === 'table') ? 'block' : 'none';
}

function exportCurrentSeatingToExcel() {
    const viewSelect = document.getElementById('view-exam-select');
    const examId = viewSelect && viewSelect.value ? parseInt(viewSelect.value) : (dashboardData.activeExamId || 0);
    downloadExcelSeating(examId);
}

function renderSeatingMatrix() {
    const filterRoom = document.getElementById('filter-room').value;
    const gridContainer = document.getElementById('seating-grid-container');
    const tableBody = document.getElementById('attendance-table-body');
    const summarySpan = document.getElementById('seating-stats-summary');

    gridContainer.innerHTML = '';
    tableBody.innerHTML = '';

    if (!currentAllocations || currentAllocations.length === 0) {
        gridContainer.innerHTML = `
            <div style="text-align: center; padding: 40px; color: var(--text-muted);">
                <h3>No Seating Arrangement Generated Yet for this Examination</h3>
                <p>Go to the <strong>Generate Seating</strong> tab to run the rule-based algorithm.</p>
            </div>
        `;
        summarySpan.innerText = '0 seats allocated';
        return;
    }

    const filtered = (filterRoom === 'ALL') ? 
        currentAllocations : 
        currentAllocations.filter(a => a.roomNumber === filterRoom);

    summarySpan.innerText = `Showing ${filtered.length} assigned seats`;

    // Group by room
    const roomGroups = {};
    filtered.forEach(a => {
        if (!roomGroups[a.roomNumber]) roomGroups[a.roomNumber] = [];
        roomGroups[a.roomNumber].push(a);
    });

    // Populate Attendance Table
    filtered.forEach(a => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${a.roomNumber}</strong></td>
            <td>Bench ${a.benchNo}</td>
            <td>Seat ${a.seatNo}</td>
            <td>${getYearBadgeHtml(a.academicYear)}</td>
            <td><strong>${a.rollNo}</strong></td>
            <td>${a.studentName}</td>
            <td>${getDivisionBadgeHtml(a.divisionName)}</td>
            <td>${a.conflictFlag === 1 ? '<span class="badge badge-warning" title="' + a.conflictReason + '">Adjacent Warning</span>' : '<span class="badge badge-success">Separated</span>'}</td>
            <td style="border-bottom: 1px dotted #94a3b8; width: 140px;">&nbsp;</td>
        `;
        tableBody.appendChild(tr);
    });

    // Populate Visual Grid
    for (const [roomNum, allocs] of Object.entries(roomGroups)) {
        const roomCard = document.createElement('div');
        roomCard.className = 'classroom-container';

        const roomMeta = allClassrooms.find(c => c.roomNumber === roomNum) || { benches: 5, seatsPerBench: 2 };

        const benchMap = {};
        for (let b = 1; b <= roomMeta.benches; b++) {
            benchMap[b] = {};
        }
        allocs.forEach(a => {
            if (!benchMap[a.benchNo]) benchMap[a.benchNo] = {};
            benchMap[a.benchNo][a.seatNo] = a;
        });

        let benchesHtml = '';
        for (let b = 1; b <= roomMeta.benches; b++) {
            let seatsHtml = '';
            for (let s = 1; s <= roomMeta.seatsPerBench; s++) {
                const assigned = benchMap[b] ? benchMap[b][s] : null;
                if (assigned) {
                    const isConflict = assigned.conflictFlag === 1;
                    seatsHtml += `
                        <div class="seat-box occupied ${isConflict ? 'conflict' : ''}" title="${isConflict ? assigned.conflictReason : 'Clean separation'}">
                            <div class="seat-label">Seat ${s}</div>
                            <div class="seat-roll">${assigned.rollNo}</div>
                            <div style="margin: 4px 0;">
                                ${getDivisionBadgeHtml(assigned.divisionName)}
                                ${getYearBadgeHtml(assigned.academicYear)}
                            </div>
                            <div class="seat-student-name" title="${assigned.studentName}">${assigned.studentName}</div>
                        </div>
                    `;
                } else {
                    seatsHtml += `
                        <div class="seat-box" style="opacity: 0.5;">
                            <div class="seat-label">Seat ${s}</div>
                            <div class="seat-roll" style="color: var(--text-muted); font-size: 0.9rem;">Empty</div>
                        </div>
                    `;
                }
            }

            benchesHtml += `
                <div class="bench-card">
                    <div class="bench-number">Bench ${b}</div>
                    <div class="seats-row">
                        ${seatsHtml}
                    </div>
                </div>
            `;
        }

        roomCard.innerHTML = `
            <div class="blackboard-banner">
                CHALKBOARD / FRONT OF CLASS: ${roomNum.toUpperCase()} (Total Benches: ${roomMeta.benches})
            </div>
            <div class="benches-grid">
                ${benchesHtml}
            </div>
        `;
        gridContainer.appendChild(roomCard);
    }
}


// DELETE ALL STUDENTS (with optional year filter)
async function confirmDeleteAll() {
    const year = (currentYearFilter !== 'All Years') ? currentYearFilter : null;
    const msg = year
        ? `⚠️ Delete all ${year} students? This will also remove their seating allocations.`
        : `⚠️ Delete ALL students from the system? This will also remove all seating allocations.\n\nThis action cannot be undone!`;

    if (!confirm(msg)) return;

    try {
        const res = await fetchApi('/students/delete-all', 'POST', { year: year || '' });
        showToast(res.message, res.success ? 'success' : 'error');
        await loadStudents();
        await loadOverviewStats();
    } catch (e) {
        showToast(e.message || 'Failed to delete students.', 'error');
    }
}
