<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="用户名/姓名/手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="组织机构">
          <el-tree-select
            v-model="searchForm.organizationId"
            :data="orgTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择组织机构"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="organizationId" label="组织机构" width="150">
          <template #default="scope">
            {{ getOrgName(scope.row.organizationId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button :type="scope.row.status === 1 ? 'warning' : 'success'" link @click="handleToggle(scope.row)">
              {{ scope.row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="userForm" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="!isAdd" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" :disabled="!isAdd" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="组织机构" prop="organizationId">
          <el-tree-select
            v-model="userForm.organizationId"
            :data="orgTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择组织机构"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="userForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isAdd = ref(true)
const tableData = ref([])
const orgTree = ref([])
const roleList = ref([])

const searchForm = reactive({
  keyword: '',
  organizationId: ''
})

const userForm = reactive({
  id: '',
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  organizationId: '',
  roleIds: [],
  status: 1
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  organizationId: [{ required: true, message: '请选择组织机构', trigger: 'change' }]
}

const dialogTitle = computed(() => isAdd.value ? '新增用户' : '编辑用户')

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.organizationId) params.organizationId = searchForm.organizationId
    
    const res = await request.get('/user/list', { params })
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const loadOrgTree = async () => {
  try {
    const res = await request.get('/organization/tree')
    orgTree.value = res.data || []
  } catch (e) {
    console.error('加载组织机构失败', e)
  }
}

const loadRoles = async () => {
  try {
    const res = await request.get('/role/list')
    roleList.value = res.data || []
  } catch (e) {
    console.error('加载角色失败', e)
  }
}

const getOrgName = (orgId) => {
  const findOrg = (nodes) => {
    for (const node of nodes) {
      if (node.id === orgId) return node.name
      if (node.children) {
        const found = findOrg(node.children)
        if (found) return found
      }
    }
    return null
  }
  return findOrg(orgTree.value) || '-'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.organizationId = ''
  loadData()
}

const handleAdd = () => {
  isAdd.value = true
  userForm.id = ''
  userForm.username = ''
  userForm.password = ''
  userForm.realName = ''
  userForm.email = ''
  userForm.phone = ''
  userForm.organizationId = ''
  userForm.roleIds = []
  userForm.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isAdd.value = false
  userForm.id = row.id
  userForm.username = row.username
  userForm.password = ''
  userForm.realName = row.realName
  userForm.email = row.email
  userForm.phone = row.phone
  userForm.organizationId = row.organizationId
  userForm.roleIds = [...(row.roleIds || [])]
  userForm.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    if (isAdd.value) {
      await request.post('/user', userForm)
      ElMessage.success('新增成功')
    } else {
      await request.put('/user', userForm)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleToggle = async (row) => {
  const action = row.status === 1 ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.put(`/user/toggle/${row.id}`)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/user/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
  loadOrgTree()
  loadRoles()
})
</script>

<style scoped>
.user-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
