// 页面切换逻辑
const pages = {
    'nav-list': 'course-list',
    'nav-add': 'course-form',
    'nav-settings': 'settings'
};

const navButtons = document.querySelectorAll('.nav-btn');
const pageElements = document.querySelectorAll('.page');

function switchPage(pageId) {
    console.log('Switching to page:', pageId);
    // 先隐藏当前页面
    const currentPage = document.querySelector('.page.active');
    if (currentPage) {
        currentPage.classList.remove('active');
        console.log('Hidden current page');
    }

    // 延迟显示新页面，实现平滑过渡
    setTimeout(() => {
        pageElements.forEach(page => page.classList.remove('active'));
        document.getElementById(pageId).classList.add('active');
        console.log('Activated page:', pageId);

        // 更新导航按钮状态
        navButtons.forEach(btn => btn.classList.remove('active'));
        document.getElementById(Object.keys(pages).find(key => pages[key] === pageId)).classList.add('active');
        console.log('Updated nav buttons');
    }, 150); // 延迟时间与过渡时间匹配
}

navButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        const pageId = pages[btn.id];
        switchPage(pageId);
    });
});

// 默认显示课程列表
switchPage('course-list');

// 视图模式切换
let currentView = 'today'; // 'today' 或 'full'

// 缩放控制
let currentZoom = 1.0; // 当前缩放比例
const minZoom = 0.5; // 最小缩放比例
const maxZoom = 2.0; // 最大缩放比例
const zoomStep = 0.1; // 缩放步长

// 缩放控制事件监听
document.getElementById('zoom-in').addEventListener('click', () => {
    if (currentZoom < maxZoom) {
        currentZoom += zoomStep;
        currentZoom = Math.min(currentZoom, maxZoom);
        updateZoom();
    }
});

document.getElementById('zoom-out').addEventListener('click', () => {
    if (currentZoom > minZoom) {
        currentZoom -= zoomStep;
        currentZoom = Math.max(currentZoom, minZoom);
        updateZoom();
    }
});

function updateZoom() {
    const zoomLevel = Math.round(currentZoom * 100);
    document.getElementById('zoom-level').textContent = `${zoomLevel}%`;
    
    // 重新渲染完整课表以应用新的缩放比例
    if (currentView === 'full') {
        renderFullSchedule();
    }
}

document.getElementById('view-today').addEventListener('click', () => {
    currentView = 'today';
    updateView();
});

document.getElementById('view-full').addEventListener('click', () => {
    currentView = 'full';
    updateView();
});

function updateView() {
    document.getElementById('view-today').classList.toggle('active', currentView === 'today');
    document.getElementById('view-full').classList.toggle('active', currentView === 'full');
    document.getElementById('page-title').textContent = currentView === 'today' ? '今日课程' : '完整课表';
    if (currentView === 'today') {
        renderCourses();
    } else {
        renderFullSchedule();
    }
}

// 窗口大小改变时重新渲染完整课表
window.addEventListener('resize', () => {
    if (currentView === 'full') {
        renderFullSchedule();
    }
});

// 课表数据（预定义课程表数据）
const predefinedSchedule = {
    1: [ // 星期一
        { period: "上午3-4节", name: "Java程序设计", location: "寸金校区1-404数", weekText: "1-16周", id: "pre_1_上午3-4节_0" }
    ],
    2: [ // 星期二
        { period: "上午3-4节", name: "概率论与数理统计", location: "寸金校区1-306数", weekText: "1-16周", id: "pre_2_上午3-4节_0" },
        { period: "下午5-6节", name: "计算机组成原理", location: "寸金校区1-403数", weekText: "1-16周", id: "pre_2_下午5-6节_0" },
        { period: "下午7-8节", name: "习近平新时代中国特色社会主义思想概论", location: "4c-805法", weekText: "1-16周", id: "pre_2_下午7-8节_0" }
    ],
    3: [ // 星期三
        { period: "上午1-2节", name: "概率论与数理统计", location: "寸金校区1-306数", weekText: "2-16周(双)", id: "pre_3_上午1-2节_0" },
        { period: "上午3-4节", name: "数值分析", location: "寸金校区1-306数", weekText: "2-8周(双)", id: "pre_3_上午3-4节_0" },
        { period: "上午3-4节", name: "数值分析(实践)", location: "寸金校区数计专用实验室", weekText: "9-15周(单)", id: "pre_3_上午3-4节_1" },
        { period: "下午5-6节", name: "Java程序设计(实践)", location: "寸金校区数计专用实验室", weekText: "1-16周", id: "pre_3_下午5-6节_0" }
    ],
    4: [ // 星期四
        { period: "上午1-2节", name: "美国概况", location: "寸金校区4C-806音", weekText: "1-16周", id: "pre_4_上午1-2节_0" },
        { period: "上午3-4节", name: "数值分析", location: "寸金校区1-403数", weekText: "1-16周", id: "pre_4_上午3-4节_0" },
        { period: "下午5-6节", name: "计算机组成原理", location: "寸金校区1-403数", weekText: "1-16周", id: "pre_4_下午5-6节_0" },
        { period: "下午7-8节", name: "乒乓球4", location: "尚武馆—乒乓球馆", weekText: "1-16周", id: "pre_4_下午7-8节_0" },
        { period: "晚上9-10节", name: "美学导论", location: "寸金校区5F-212食", weekText: "9-16周", id: "pre_4_晚上9-10节_0" }
    ],
    5: [ // 星期五
        { period: "上午1-2节", name: "Python程序设计", location: "寸金校区数计专用实验室", weekText: "1-8周", id: "pre_5_上午1-2节_0" },
        { period: "上午3-4节", name: "Python程序设计(实践)", location: "寸金校区数计专用实验室", weekText: "9-16周", id: "pre_5_上午3-4节_0" },
        { period: "下午5-6节", name: "数学建模", location: "寸金校区数计专用实验室", weekText: "1-12周", id: "pre_5_下午5-6节_0" },
        { period: "下午7-8节", name: "数学建模(实践)", location: "寸金校区数计专用实验室", weekText: "1-8周/9-12周", id: "pre_5_下午7-8节_0" }
    ],
    6: [ // 星期六
        { period: "上午1-2节", name: "形势与政策Ⅳ", location: "寸金校区3-603", weekText: "13周", id: "pre_6_上午1-2节_0" },
        { period: "上午3-4节", name: "形势与政策Ⅳ", location: "寸金校区3-603", weekText: "11-12周", id: "pre_6_上午3-4节_0" }
    ],
    7: [ // 星期日
        // 星期日没有课程
    ]
};

// 课表数据（合并预定义和自定义课程）
let schedule = {};

// 初始化课表数据
function initializeSchedule() {
    // 清理localStorage中错误保存的预定义课程
    cleanupLocalStorage();
    
    // 深拷贝预定义课表，过滤掉已删除的
    const deletedPredefined = JSON.parse(localStorage.getItem('deletedPredefined')) || [];
    schedule = {};
    Object.keys(predefinedSchedule).forEach(day => {
        schedule[day] = predefinedSchedule[day].filter(lesson => !deletedPredefined.includes(lesson.id));
    });

    // 加载自定义课程并合并
    loadCustomSchedule();
}

// 清理localStorage中错误保存的预定义课程
function cleanupLocalStorage() {
    const customSchedule = JSON.parse(localStorage.getItem('customSchedule')) || {};
    let hasChanges = false;
    
    Object.keys(customSchedule).forEach(day => {
        const originalLength = customSchedule[day].length;
        // 移除没有ID或ID以pre_开头的课程
        customSchedule[day] = customSchedule[day].filter(lesson => {
            if (!lesson.id) return false;
            if (typeof lesson.id === 'string' && lesson.id.startsWith('pre_')) return false;
            return true;
        });
        
        // 检查过滤后是否还有课程
        if (customSchedule[day] && customSchedule[day].length === 0) {
            delete customSchedule[day];
        }
        
        // 检查是否有变化
        if (customSchedule[day] && customSchedule[day].length !== originalLength) {
            hasChanges = true;
        }
    });
    
    if (hasChanges) {
        console.log('清理了localStorage中的错误数据');
        localStorage.setItem('customSchedule', JSON.stringify(customSchedule));
    }
}

// 从localStorage加载自定义课表并合并
function loadCustomSchedule() {
    const customSchedule = JSON.parse(localStorage.getItem('customSchedule')) || {};
    Object.keys(customSchedule).forEach(day => {
        const dayNum = parseInt(day);
        if (!schedule[dayNum]) {
            schedule[dayNum] = [];
        }
        // 避免重复添加（通过id检查）
        const existingIds = schedule[dayNum].map(l => l.id).filter(id => id);
        const newLessons = customSchedule[day].filter(lesson => !existingIds.includes(lesson.id));
        schedule[dayNum].push(...newLessons);
    });
}

const TERM_START = new Date(2026, 2, 2); // 2026-03-02，第一周（单周）
const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];

function getWeekInfo(date) {
    const dayMs = 24 * 60 * 60 * 1000;
    const diffDays = Math.floor((date - TERM_START) / dayMs);
    const weekNum = Math.floor(diffDays / 7) + 1;
    const isOdd = weekNum % 2 === 1;
    return { weekNum, isOdd };
}

function isWeekActive(weekText, weekNum) {
    // 支持格式："1-16周"、"2-16周(双)"、"9周"、"1-8周/9-12周" 等
    const parts = weekText.split('/').map(p => p.trim());
    return parts.some(part => {
        const match = part.match(/^(\d+)(?:-(\d+))?周(?:\((单|双)\))?$/);
        if (!match) return false;
        const start = parseInt(match[1], 10);
        const end = match[2] ? parseInt(match[2], 10) : start;
        const parity = match[3];
        if (weekNum < start || weekNum > end) return false;
        if (!parity) return true;
        if (parity === '单') return weekNum % 2 === 1;
        if (parity === '双') return weekNum % 2 === 0;
        return true;
    });
}

function renderCourses() {
    const now = new Date();
    const { weekNum, isOdd } = getWeekInfo(now);

    document.getElementById('current-date').textContent = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
    document.getElementById('current-weekday').textContent = weekdays[now.getDay()];
    document.getElementById('week-info').textContent = `第${weekNum}周（${isOdd ? '单周' : '双周'}）`;

    const todayIndex = now.getDay() === 0 ? 7 : now.getDay();
    const todayLessons = (schedule[todayIndex] || []).filter(item => isWeekActive(item.weekText, weekNum));

    const coursesContainer = document.getElementById('courses');
    coursesContainer.innerHTML = '';

    if (!Object.keys(schedule).length) {
        coursesContainer.innerHTML = `
            <div class="no-courses-container">
                <div class="no-courses-icon">⚠️</div>
                <h3 class="no-courses-title">课表未加载</h3>
                <p class="no-courses-subtitle">请确认已将页面在 Web 服务器上打开，或课表文件可正常访问。</p>
            </div>
        `;
        return;
    }

    if (todayLessons.length === 0) {
        coursesContainer.innerHTML = `
            <div class="no-courses-container">
                <div class="no-courses-icon">🎉</div>
                <h3 class="no-courses-title">今日无课程</h3>
                <p class="no-courses-subtitle">享受你的自由时光吧！</p>
                <div class="no-courses-decoration"></div>
            </div>
        `;
    } else {
        todayLessons.forEach(lesson => {
            const courseItem = document.createElement('div');
            courseItem.className = 'course-item';
            courseItem.innerHTML = `
                <div class="course-content">
                    <h3>${lesson.name}</h3>
                    <p>${lesson.period} · ${lesson.location}</p>
                    <p class="week-tag">${lesson.weekText}</p>
                </div>
            `;
            coursesContainer.appendChild(courseItem);
        });
    }
}

// 检测并合并连续的课程
function mergeConsecutiveLessons(allDayLessons, day, currentPeriod) {
    // 检查当前period是否有课程
    const currentLesson = allDayLessons.find(l => l.period === currentPeriod);
    if (!currentLesson) return [];

    const periodOrder = ['上午1-2节', '上午3-4节', '下午5-6节', '下午7-8节', '晚上9-10节'];
    const currentIndex = periodOrder.indexOf(currentPeriod);

    // 检查这是否是一个连续课程组的起始点
    const isStartOfConsecutiveGroup = (() => {
        // 如果这是第一个period，或者前一个period没有相同的课程
        if (currentIndex === 0) return true;

        const prevPeriod = periodOrder[currentIndex - 1];
        const prevLesson = allDayLessons.find(l =>
            l.period === prevPeriod &&
            l.name === currentLesson.name &&
            l.location === currentLesson.location &&
            l.weekText === currentLesson.weekText
        );

        return !prevLesson; // 如果前一个period没有相同的课程，这就开始一个新组
    })();

    if (!isStartOfConsecutiveGroup) {
        // 这不是连续组的起始点，不显示
        return [];
    }

    // 查找连续的课程
    const consecutiveLessons = [currentLesson];
    let nextPeriodIndex = currentIndex + 1;

    while (nextPeriodIndex < periodOrder.length) {
        const nextPeriod = periodOrder[nextPeriodIndex];
        const nextLesson = allDayLessons.find(l =>
            l.name === currentLesson.name &&
            l.location === currentLesson.location &&
            l.weekText === currentLesson.weekText &&
            l.period === nextPeriod
        );

        if (nextLesson) {
            consecutiveLessons.push(nextLesson);
            nextPeriodIndex++;
        } else {
            break;
        }
    }

    if (consecutiveLessons.length > 1) {
        // 合并连续的课程
        return [{
            isMerged: true,
            name: currentLesson.name,
            location: currentLesson.location,
            weekText: currentLesson.weekText,
            periods: consecutiveLessons.map(l => l.period),
            lessons: consecutiveLessons
        }];
    } else {
        // 单个课程
        return [{
            isMerged: false,
            lessons: [currentLesson]
        }];
    }
}

function renderFullSchedule() {
    const now = new Date();
    const { weekNum } = getWeekInfo(now);

    const coursesContainer = document.getElementById('courses');
    coursesContainer.innerHTML = '';

    if (!Object.keys(schedule).length) {
        coursesContainer.innerHTML = `
            <div class="no-courses-container">
                <div class="no-courses-icon">⚠️</div>
                <h3 class="no-courses-title">课表未加载</h3>
                <p class="no-courses-subtitle">请确认已将页面在 Web 服务器上打开，或课表文件可正常访问。</p>
            </div>
        `;
        return;
    }

    // 创建表格包装容器
    const tableWrapper = document.createElement('div');
    tableWrapper.className = 'table-wrapper';

    // 添加提示文字
    const hintText = document.createElement('p');
    hintText.className = 'edit-hint';
    hintText.textContent = '双击修改课程哦';
    tableWrapper.appendChild(hintText);

    // 创建表格
    const table = document.createElement('table');
    table.className = 'timetable';

    // 使用当前缩放比例
    table.style.setProperty('--table-scale', currentZoom);

    // 表头
    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    const headers = ['上课节次', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'];
    headers.forEach(header => {
        const th = document.createElement('th');
        th.textContent = header;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    // 表体
    const tbody = document.createElement('tbody');
    const periods = ['上午1-2节', '上午3-4节', '下午5-6节', '下午7-8节', '晚上9-10节'];

    periods.forEach(period => {
        const row = document.createElement('tr');
        const periodCell = document.createElement('td');
        periodCell.textContent = period;
        row.appendChild(periodCell);

        for (let day = 1; day <= 7; day++) {
            const cell = document.createElement('td');
            // 获取这一天这一节次的所有课程（不过滤周次）
            const allLessons = (schedule[day] || []).filter(lesson => lesson.period === period);

            if (allLessons.length === 0) {
                cell.textContent = '-';
            } else if (allLessons.length === 1) {
                const lesson = allLessons[0];
                const nameDiv = document.createElement('div');
                nameDiv.className = 'class-card';
                nameDiv.textContent = lesson.name;
                nameDiv.addEventListener('dblclick', () => {
                    console.log('双击课程:', lesson.name, '星期:', day, '节次:', period);
                    editCourse(lesson, day, period);
                });
                let touchCount = 0;
                nameDiv.addEventListener('touchend', (e) => {
                    touchCount++;
                    setTimeout(() => { touchCount = 0; }, 300);
                    if (touchCount === 2) {
                        console.log('触摸双击课程:', lesson.name, '星期:', day, '节次:', period);
                        editCourse(lesson, day, period);
                        e.preventDefault();
                    }
                });
                cell.appendChild(nameDiv);

                const locationDiv = document.createElement('div');
                locationDiv.className = 'class-card';
                locationDiv.textContent = lesson.location;
                locationDiv.addEventListener('dblclick', () => {
                    console.log('双击地点:', lesson.location, '星期:', day, '节次:', period);
                    editCourse(lesson, day, period);
                });
                let locationTouchCount = 0;
                locationDiv.addEventListener('touchend', (e) => {
                    locationTouchCount++;
                    setTimeout(() => { locationTouchCount = 0; }, 300);
                    if (locationTouchCount === 2) {
                        console.log('触摸双击地点:', lesson.location, '星期:', day, '节次:', period);
                        editCourse(lesson, day, period);
                        e.preventDefault();
                    }
                });
                cell.appendChild(locationDiv);

                const weekDiv = document.createElement('div');
                weekDiv.className = 'class-card week-tag';
                weekDiv.textContent = lesson.weekText;
                weekDiv.addEventListener('dblclick', () => {
                    console.log('双击周次:', lesson.weekText, '星期:', day, '节次:', period);
                    editCourse(lesson, day, period);
                });
                let weekTouchCount = 0;
                weekDiv.addEventListener('touchend', (e) => {
                    weekTouchCount++;
                    setTimeout(() => { weekTouchCount = 0; }, 300);
                    if (weekTouchCount === 2) {
                        console.log('触摸双击周次:', lesson.weekText, '星期:', day, '节次:', period);
                        editCourse(lesson, day, period);
                        e.preventDefault();
                    }
                });
                cell.appendChild(weekDiv);
            } else {
                // 多个课程，循环显示
                const container = document.createElement('div');
                container.className = 'course-container';
                container.dataset.currentIndex = '0';

                allLessons.forEach((lesson, index) => {
                    const lessonDiv = document.createElement('div');
                    lessonDiv.className = 'lesson-group';
                    lessonDiv.style.display = index === 0 ? 'block' : 'none';

                    const nameDiv = document.createElement('div');
                    nameDiv.className = 'class-card';
                    nameDiv.textContent = lesson.name;
                    lessonDiv.appendChild(nameDiv);

                    const locationDiv = document.createElement('div');
                    locationDiv.className = 'class-card';
                    locationDiv.textContent = lesson.location;
                    lessonDiv.appendChild(locationDiv);

                    const weekDiv = document.createElement('div');
                    weekDiv.className = 'class-card week-tag';
                    weekDiv.textContent = lesson.weekText;
                    lessonDiv.appendChild(weekDiv);

                    container.appendChild(lessonDiv);
                });

                cell.appendChild(container);

                // 循环显示
                let currentIndex = 0;
                const lessonGroups = container.querySelectorAll('.lesson-group');
                setInterval(() => {
                    lessonGroups[currentIndex].style.display = 'none';
                    currentIndex = (currentIndex + 1) % lessonGroups.length;
                    lessonGroups[currentIndex].style.display = 'block';
                    container.dataset.currentIndex = currentIndex.toString();
                }, 1000); // 每秒切换

                // 双击编辑当前显示的课程
                container.addEventListener('dblclick', () => {
                    const currentIndex = parseInt(container.dataset.currentIndex || 0);
                    const lesson = allLessons[currentIndex];
                    console.log('双击容器，编辑课程:', lesson.name, '星期:', day, '节次:', period);
                    editCourse(lesson, day, period);
                });

                let containerTouchCount = 0;
                container.addEventListener('touchend', (e) => {
                    containerTouchCount++;
                    setTimeout(() => { containerTouchCount = 0; }, 300);
                    if (containerTouchCount === 2) {
                        const currentIndex = parseInt(container.dataset.currentIndex || 0);
                        const lesson = allLessons[currentIndex];
                        console.log('触摸双击容器，编辑课程:', lesson.name, '星期:', day, '节次:', period);
                        editCourse(lesson, day, period);
                        e.preventDefault();
                    }
                });
            }
            row.appendChild(cell);
        }
        tbody.appendChild(row);
    });
    table.appendChild(tbody);
    tableWrapper.appendChild(table);
    coursesContainer.appendChild(tableWrapper);
    console.log('renderFullSchedule completed, table created with', periods.length, 'periods');
}

// 全局变量用于编辑
let editingCourse = null;

function editCourse(lesson, weekday, period) {
    console.log('editCourse called with:', lesson, weekday, period);
    if (!lesson) {
        alert('未找到要编辑的课程');
        return;
    }

    editingCourse = { lesson, weekday, period };

    // 先切换页面，然后延迟设置表单值
    console.log('Switching to course-form page');
    switchPage('course-form');

    // 延迟设置表单值，确保页面切换完成
    setTimeout(() => {
        console.log('Setting form values after page switch');
        document.getElementById('courseName').value = lesson.name;
        document.getElementById('courseLocation').value = lesson.location;
        document.getElementById('weekday').value = weekday.toString();
        document.getElementById('period').value = period;
        console.log('Setting weekday to:', weekday.toString(), 'period to:', period);

        // 解析周次信息
        const weekMatch = lesson.weekText.match(/^(\d+)-(\d+)周(?:\((单|双)\))?$/);
        if (weekMatch) {
            document.getElementById('startWeek').value = weekMatch[1];
            document.getElementById('endWeek').value = weekMatch[2];
            const weekType = weekMatch[3];
            let weekTypeValue = 'all';
            if (weekType === '单') {
                weekTypeValue = 'odd';
            } else if (weekType === '双') {
                weekTypeValue = 'even';
            }
            document.getElementById('weekType').value = weekTypeValue;
            console.log('Setting weekType to:', weekTypeValue);
        }

        document.getElementById('form-title').textContent = '编辑课程';
        document.getElementById('submitBtn').textContent = '保存修改';
        document.getElementById('deleteBtn').style.display = 'inline-block';
        console.log('Form setup complete');
    }, 200); // 比页面切换延迟稍长一些
}

// 重置表单状态
function resetForm() {
    // 手动重置表单元素而不是使用reset()
    document.getElementById('courseName').value = '';
    document.getElementById('courseLocation').value = '';
    document.getElementById('weekday').value = '';
    document.getElementById('period').value = '';
    document.getElementById('weekType').value = '';
    document.getElementById('startWeek').value = '';
    document.getElementById('endWeek').value = '';

    editingCourse = null;
    document.getElementById('form-title').textContent = '添加课程';
    document.getElementById('submitBtn').textContent = '添加课程';
    document.getElementById('deleteBtn').style.display = 'none';
}

document.getElementById('courseForm').addEventListener('submit', (e) => {
    e.preventDefault();

    const name = document.getElementById('courseName').value.trim();
    const location = document.getElementById('courseLocation').value.trim();
    const weekday = parseInt(document.getElementById('weekday').value);
    const period = document.getElementById('period').value;
    const weekType = document.getElementById('weekType').value;
    const startWeek = parseInt(document.getElementById('startWeek').value);
    const endWeek = parseInt(document.getElementById('endWeek').value);

    // 验证输入
    if (!name || !location || !weekday || !period || !weekType || !startWeek || !endWeek) {
        alert('请填写所有必填字段');
        return;
    }

    if (startWeek > endWeek) {
        alert('起始周不能大于结束周');
        return;
    }

    // 生成周数文本
    let weekText = `${startWeek}-${endWeek}周`;
    if (weekType === 'odd') {
        weekText += '(单)';
    } else if (weekType === 'even') {
        weekText += '(双)';
    }

    // 检查冲突
    const conflict = checkCourseConflict(weekday, period, weekText);
    if (conflict) {
        alert(`课程冲突！\n现有课程: ${conflict.name}\n时间: ${conflict.period}\n地点: ${conflict.location}\n周次: ${conflict.weekText}`);
        return;
    }

    // 创建课程对象
    const courseData = {
        name,
        location,
        weekday,
        period,
        weekText
    };

    // 如果是编辑模式，删除原有课程并保留ID
    if (editingCourse) {
        const { lesson: oldLesson, weekday: oldWeekday, period: oldPeriod } = editingCourse;
        // 保留原有ID（如果是预定义课程，保留pre_开头的ID；如果是自定义课程，保留数字ID）
        if (oldLesson.id) {
            courseData.id = oldLesson.id;
        } else {
            courseData.id = Date.now();
        }
    } else {
        // 新添加的课程，生成新ID
        courseData.id = Date.now();
    }

    // 添加到schedule
    if (!schedule[weekday]) {
        schedule[weekday] = [];
    }
    schedule[weekday].push(courseData);

    // 保存到localStorage
    saveSchedule();

    const action = editingCourse ? '修改' : '添加';
    alert(`课程${action}成功！`);

    // 重置表单状态
    resetForm();
    switchPage('course-list');
    updateView();
});

// 取消按钮
document.getElementById('cancelBtn').addEventListener('click', () => {
    resetForm();
    switchPage('course-list');
});

// 删除按钮
document.getElementById('deleteBtn').addEventListener('click', () => {
    if (!editingCourse) return;

    if (!confirm('确定删除此课程？')) {
        return;
    }

    const { lesson, weekday, period } = editingCourse;

    console.log('删除课程信息:', { lesson, weekday, period, lessonId: lesson.id });

    // 查找并删除课程
    const dayLessons = schedule[weekday] || [];
    const index = dayLessons.findIndex(l => l.name === lesson.name && l.location === lesson.location && l.period === period && l.weekText === lesson.weekText);

    if (index === -1) {
        alert('未找到要删除的课程');
        return;
    }

    const deletedLesson = dayLessons.splice(index, 1)[0];

    console.log('已删除的课程:', deletedLesson);
    console.log('课程ID类型:', typeof deletedLesson.id);
    console.log('课程ID值:', deletedLesson.id);
    console.log('是否以pre_开头:', typeof deletedLesson.id === 'string' && deletedLesson.id.startsWith('pre_'));

    // 判断是否是预定义课程
    const isPredefinedLesson = deletedLesson.id && typeof deletedLesson.id === 'string' && deletedLesson.id.startsWith('pre_');
    
    // 如果是预定义课程，保存到deletedPredefined
    if (isPredefinedLesson) {
        console.log('进入预定义课程删除逻辑');
        const deletedPredefined = JSON.parse(localStorage.getItem('deletedPredefined')) || [];
        if (!deletedPredefined.includes(deletedLesson.id)) {
            deletedPredefined.push(deletedLesson.id);
            localStorage.setItem('deletedPredefined', JSON.stringify(deletedPredefined));
            console.log('预定义课程已添加到删除列表:', deletedLesson.id);
        }
    }

    // 保存自定义课程
    saveSchedule();

    // 重新初始化schedule以确保数据一致性
    initializeSchedule();

    alert('课程删除成功！');

    // 重置表单状态
    resetForm();
    switchPage('course-list');
    updateView();
});

// 检查课程冲突
function checkCourseConflict(weekday, period, weekText) {
    const dayLessons = schedule[weekday] || [];
    const now = new Date();
    const { weekNum } = getWeekInfo(now);

    for (const lesson of dayLessons) {
        if (lesson.period === period && isWeekActive(weekText, weekNum) && isWeekActive(lesson.weekText, weekNum)) {
            return lesson;
        }
    }
    return null;
}

// 保存课表到localStorage
function saveSchedule() {
    const customSchedule = {};
    Object.keys(schedule).forEach(day => {
        const dayKey = String(day);  // 确保使用字符串key
        // 只保存自定义课程（ID不是以pre_开头的字符串）
        customSchedule[dayKey] = schedule[day].filter(lesson => {
            // 如果ID是字符串且以pre_开头，说明是预定义课程，不保存
            if (typeof lesson.id === 'string' && lesson.id.startsWith('pre_')) return false;
            // 如果没有ID，说明是预定义课程，不保存
            if (!lesson.id) return false;
            // 否则是自定义课程，保存
            return true;
        });
    });
    localStorage.setItem('customSchedule', JSON.stringify(customSchedule));
}

// 设置管理
const settings = JSON.parse(localStorage.getItem('settings')) || { theme: 'light' };

// 初始化主题
function initTheme() {
    const theme = settings.theme || 'light';
    applyTheme(theme);
    
    // 设置主题选择器的状态
    const themeOptions = document.querySelectorAll('.theme-option');
    themeOptions.forEach(option => {
        if (option.dataset.theme === theme) {
            option.classList.add('active');
        } else {
            option.classList.remove('active');
        }
    });
}

// 应用主题
function applyTheme(theme) {
    if (theme === 'dark') {
        document.body.classList.add('dark-mode');
    } else {
        document.body.classList.remove('dark-mode');
    }
}

// 主题选择器点击事件
document.querySelectorAll('.theme-option').forEach(option => {
    option.addEventListener('click', () => {
        const selectedTheme = option.dataset.theme;
        settings.theme = selectedTheme;
        localStorage.setItem('settings', JSON.stringify(settings));
        
        // 更新UI状态
        document.querySelectorAll('.theme-option').forEach(opt => {
            opt.classList.remove('active');
        });
        option.classList.add('active');
        
        // 应用主题
        applyTheme(selectedTheme);
        
        alert('主题已切换为' + (selectedTheme === 'dark' ? '深色模式' : '浅色模式'));
    });
});

// 初始渲染
(async function init() {
    console.log('Initializing app...');
    initializeSchedule();
    console.log('Schedule initialized, updating view...');
    updateView();
    console.log('App initialization complete');
    
    // 初始化主题
    initTheme();
})();
