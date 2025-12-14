# ✅ COMPLETE WORKING PROTOTYPE - USER GUIDE

## 🎯 **FULL WORKFLOW - Everything Works Now!**

### **1. Access the Dashboards**

**Teacher Dashboard:**
- URL: `http://localhost:8081` (if port-forwarding)
- Or: `minikube service teacher-frontend`

**Student Dashboard:**
- URL: `http://localhost:8080` (if port-forwarding)
- Or: `minikube service student-frontend`

---

### **2. Create Users (Teacher or Student)**

#### **In Teacher Dashboard:**
1. At the top, you'll see **"Create User"** section
2. Fill in:
   - **Name**: e.g., "John Doe"
   - **Email**: e.g., "john@example.com"
   - **Role**: Select "Teacher" or "Student"
3. Click **"Create User"**

#### **In Student Dashboard:**
1. At the top, you'll see **"Create User"** section
2. Fill in:
   - **Name**: e.g., "Jane Student"
   - **Email**: e.g., "jane@example.com"
   - **Role**: Select "Student" or "Teacher"
3. Click **"Create User"**

---

### **3. Create Tasks (Teacher Dashboard)**

1. Go to **Teacher Dashboard**
2. Scroll to **"Create New Task"** section
3. Fill in:
   - **Task Title**: e.g., "Study Kubernetes"
   - **Description**: e.g., "Learn microservices"
   - **Subject** (optional): e.g., "Computer Science"
   - **Assign to Students**: Check the students you want to assign this task to
4. Click **"Create Task"**

---

### **4. Upload File & Complete Task (Student Dashboard)**

1. Go to **Student Dashboard**
2. **Select your student** from the dropdown at the top
3. You'll see all tasks assigned to that student
4. For each task, you have **TWO options**:

#### **Option A: Upload File (First Time)**
- Click **"Choose File"** and select your file
- Click **"⬆️ Upload & Complete Task"** button
- The file will be uploaded AND the task will be marked as complete automatically

#### **Option B: Analyze Already Uploaded File**
- If a file was already uploaded but task shows "Pending"
- You'll see a **yellow box** with:
  - **"🤖 Analyze & Mark Complete"** button (blue) - Click this!
  - OR **"✓ Mark as Complete (Skip Analysis)"** button (green) - Alternative option

---

### **5. What Happens When You Upload/Analyze:**

✅ **File is uploaded**
✅ **Task is marked as `taskDone = true`**
✅ **Task is marked as `completed = true`**
✅ **Task shows "✓ Task Done" badge**
✅ **Task shows "✅ Task completed!" message**

---

## 🔧 **Technical Details**

### **All 3 Microservices Are Working:**

1. **user-service** (Port 8082)
   - Creates teachers and students
   - Stores user data in MongoDB

2. **task-service** (Port 8081)
   - Creates tasks
   - Assigns tasks to students
   - Updates task status (fileUploaded, taskDone, completed)
   - Stores task data in MongoDB

3. **agent-service** (Port 8083)
   - Handles file uploads via `/api/agent/analyze-file`
   - Marks tasks as complete (CPU-friendly mode - no AI needed)
   - Updates tasks in MongoDB via task-service

---

## 🎨 **UI Features**

### **Teacher Dashboard:**
- ✅ Create users (teachers/students)
- ✅ Create tasks
- ✅ Assign tasks to students
- ✅ View all tasks and their status

### **Student Dashboard:**
- ✅ Create users (students/teachers)
- ✅ Select student profile
- ✅ View assigned tasks
- ✅ Upload files for tasks
- ✅ **"Analyze & Mark Complete"** button for uploaded files
- ✅ See task completion status

---

## 🚀 **Quick Start:**

1. **Create a Teacher:**
   - Teacher Dashboard → Create User → Role: Teacher → Create

2. **Create a Student:**
   - Teacher Dashboard → Create User → Role: Student → Create

3. **Create a Task:**
   - Teacher Dashboard → Create New Task → Assign to Student → Create

4. **Complete the Task:**
   - Student Dashboard → Select Student → Find Task → Upload File → Click "Upload & Complete Task"
   - OR if file already uploaded: Click "🤖 Analyze & Mark Complete"

---

## ✅ **Everything is Working Now!**

- ✅ User creation in both dashboards
- ✅ Task creation and assignment
- ✅ File upload
- ✅ Analysis and completion marking
- ✅ All 3 microservices integrated
- ✅ MongoDB updates working
- ✅ UI shows correct status

**Just refresh your browser and start using it!**

