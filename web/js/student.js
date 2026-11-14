/**
 * Student Portal Controller - ExamSeat Pro
 */

let currentAllocation = null;

document.addEventListener('DOMContentLoaded', async () => {
    const user = getUser();
    if (!user) {
        window.location.href = 'index.html';
        return;
    }

    const name = user.name || 'Student';
    document.getElementById('student-welcome-name').innerText = `Welcome, ${name}`;
    document.getElementById('student-subtext').innerText = `Roll No: ${user.rollNo || user.username} | Year: ${user.academicYear || 'Third Year'} | Division: ${user.divisionName || 'N/A'}`;
    
    const initials = name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    document.getElementById('student-avatar-initials').innerText = initials || 'S';

    await loadStudentSeating();
});

async function loadStudentSeating() {
    const loadingState = document.getElementById('loading-state');
    const hallTicket = document.getElementById('hall-ticket-container');
    const noSeatingAlert = document.getElementById('no-seating-alert');

    loadingState.style.display = 'block';
    hallTicket.style.display = 'none';
    noSeatingAlert.style.display = 'none';

    try {
        const res = await fetchApi('/student/seating');
        currentAllocation = res;

        // Populate Pass
        document.getElementById('pass-room').innerText = res.roomNumber;
        document.getElementById('pass-bench-seat').innerText = `Bench ${res.benchNo}  •  Seat ${res.seatNo}`;
        document.getElementById('pass-name').innerText = res.studentName;
        document.getElementById('pass-roll').innerText = res.rollNo;
        document.getElementById('pass-year').innerHTML = getYearBadgeHtml(res.academicYear);
        document.getElementById('pass-division').innerHTML = getDivisionBadgeHtml(res.divisionName);
        document.getElementById('pass-header-exam-name').innerText = res.examName || 'Official Examination Seating Pass';
        document.getElementById('pass-subject').innerText = res.examSubject || 'Major Course Examination';
        document.getElementById('pass-date').innerText = res.examDate || '-';
        document.getElementById('pass-time').innerText = res.examTime || (res.startTime ? `${res.startTime} - ${res.endTime}` : '-');

        loadingState.style.display = 'none';
        hallTicket.style.display = 'block';

        // Render Classroom Mini-Map
        await renderStudentMinimap(res);

    } catch (e) {
        loadingState.style.display = 'none';
        noSeatingAlert.style.display = 'block';
    }
}

async function renderStudentMinimap(studentAlloc) {
    const container = document.getElementById('student-minimap-content');
    container.innerHTML = '<div style="text-align: center; color: var(--text-muted);">Loading classroom floor map...</div>';

    try {
        const [classrooms, roomAllocs] = await Promise.all([
            fetchApi('/classrooms'),
            fetchApi(`/seating/exam?examId=${studentAlloc.examId}`)
        ]);

        const room = classrooms.find(c => c.roomNumber === studentAlloc.roomNumber) || {
            benches: 5,
            seatsPerBench: 2
        };

        document.getElementById('minimap-badge').innerText = `${studentAlloc.roomNumber} Floor Plan`;

        // Map existing allocations for this room
        const benchMap = {};
        for (let b = 1; b <= room.benches; b++) {
            benchMap[b] = {};
        }
        roomAllocs.filter(a => a.roomNumber === studentAlloc.roomNumber).forEach(a => {
            if (!benchMap[a.benchNo]) benchMap[a.benchNo] = {};
            benchMap[a.benchNo][a.seatNo] = a;
        });

        let benchesHtml = '';
        for (let b = 1; b <= room.benches; b++) {
            let seatsHtml = '';
            for (let s = 1; s <= room.seatsPerBench; s++) {
                const isMySeat = (b === studentAlloc.benchNo && s === studentAlloc.seatNo);
                const assigned = benchMap[b] ? benchMap[b][s] : null;

                if (isMySeat) {
                    seatsHtml += `
                        <div class="seat-box highlighted" style="background: #fef3c7; border: 2px solid #d97706;">
                            <div class="seat-label" style="color: #b45309; font-weight: 800;">★ YOUR SEAT ★</div>
                            <div class="seat-roll" style="color: #92400e;">${studentAlloc.rollNo}</div>
                            <div style="margin: 4px 0;">
                                ${getDivisionBadgeHtml(studentAlloc.divisionName)}
                                ${getYearBadgeHtml(studentAlloc.academicYear)}
                            </div>
                            <div class="seat-student-name" style="font-weight: 700; color: #78350f;">(YOU)</div>
                        </div>
                    `;
                } else if (assigned) {
                    seatsHtml += `
                        <div class="seat-box occupied" style="opacity: 0.85;">
                            <div class="seat-label">Seat ${s}</div>
                            <div class="seat-roll" style="font-size: 0.95rem;">${assigned.rollNo}</div>
                            <div>
                                ${getDivisionBadgeHtml(assigned.divisionName)}
                            </div>
                        </div>
                    `;
                } else {
                    seatsHtml += `
                        <div class="seat-box" style="opacity: 0.4;">
                            <div class="seat-label">Seat ${s}</div>
                            <div class="seat-roll" style="font-size: 0.85rem; color: var(--text-muted);">Empty</div>
                        </div>
                    `;
                }
            }

            benchesHtml += `
                <div class="bench-card" ${b === studentAlloc.benchNo ? 'style="border: 2px solid #f59e0b; background: #fffdf5;"' : ''}>
                    <div class="bench-number" ${b === studentAlloc.benchNo ? 'style="color: #d97706; font-weight: 800;"' : ''}>
                        ${b === studentAlloc.benchNo ? '👉 YOUR BENCH ' + b : 'Bench ' + b}
                    </div>
                    <div class="seats-row">
                        ${seatsHtml}
                    </div>
                </div>
            `;
        }

        container.innerHTML = `
            <div class="blackboard-banner">
                CHALKBOARD / FRONT OF CLASS (${studentAlloc.roomNumber})
            </div>
            <div class="benches-grid">
                ${benchesHtml}
            </div>
        `;

    } catch (e) {
        container.innerHTML = `<div style="color: var(--danger);">Could not render map: ${e.message}</div>`;
    }
}
