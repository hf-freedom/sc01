<template>
  <div class="application-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>应用管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增应用
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="应用名称/描述" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="应用类型">
          <el-select v-model="searchForm.type" placeholder="请选择应用类型" clearable>
            <el-option label="类型0" :value="0" />
            <el-option label="类型1" :value="1" />
          </el-select>
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
        <el-table-column prop="name" label="应用名称" width="200" />
        <el-table-column prop="type" label="应用类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.type === 0 ? 'primary' : 'success'">
              类型{{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="organizationId" label="所属机构" width="180">
          <template #default="scope">
            {{ getOrgName(scope.row.organizationId) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="应用描述" min-width="200" />
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="appForm" :rules="appRules" label-width="100px">
        <el-form-item label="应用名称" prop="name">
          <el-input v-model="appForm.name" placeholder="请输入应用名称" />
        </el-form-item>
        <el-form-item label="应用类型" prop="type">
          <el-radio-group v-model="appForm.type">
            <el-radio :label="0">类型0</el-radio>
            <el-radio :label="1">类型1</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="组织机构" prop="organizationId">
          <el-tree-select
            v-model="appForm.organizationId"
            :data="orgTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择组织机构"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="appForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="appForm.description" type="textarea" :rows="3" placeholder="请输入应用描述" />
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

const searchForm = reactive({
  keyword: '',
  type: null,
  organizationId: ''
})

const appForm = reactive({
  id: '',
  name: '',
  type: 0,
  organizationId: '',
  status: 1,
  description: ''
})

const appRules = {
  name: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择应用类型', trigger: 'change' }],
  organizationId: [{ required: true, message: '请选择组织机构', trigger: 'change' }]
}

const dialogTitle = computed(() => isAdd.value ? '新增应用' : '编辑应用')

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.type !== null) params.type = searchForm.type
    if (searchForm.organizationId) params.organizationId = searchForm.organizationId
    
    const res = await request.get('/application/list', { params })
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
  searchForm.type = null
  searchForm.organizationId = ''
  loadData()
}

const handleAdd = () => {
  isAdd.value = true
  appForm.id = ''
  appForm.name = ''
  appForm.type = 0
  appForm.organizationId = ''
  appForm.status = 1
  appForm.description = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isAdd.value = false
  appForm.id = row.id
  appForm.name = row.name
  appForm.type = row.type
  appForm.organizationId = row.organizationId
  appForm.status = row.status
  appForm.description = row.description
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    if (isAdd.value) {
      await request.post('/application', appForm)
      ElMessage.success('新增成功')
    } else {
      await request.put('/application', appForm)
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
    await ElMessageBox.confirm(`确定要${action}该应用吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.put(`/application/toggle/${row.id}`)
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
    await ElMessageBox.confirm('确定要删除该应用吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/application/${row.id}`)
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
})
</script>

<style scoped>
.application-list {
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
