<template>
  <div class="role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="角色名称/编码" clearable @keyup.enter="handleSearch" />
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
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="code" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
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
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" link @click="handleBindResource(scope.row)">绑定资源</el-button>
            <el-button type="warning" link @click="handleBindUser(scope.row)">绑定用户</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="roleForm" :rules="roleRules" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="roleForm.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="组织机构" prop="organizationId">
          <el-tree-select
            v-model="roleForm.organizationId"
            :data="orgTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择组织机构"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="绑定资源" v-model="resourceDialogVisible" width="600px">
      <el-tree
        ref="resourceTreeRef"
        :data="resourceTree"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedResourceIds"
        :default-expanded-keys="expandedResourceIds"
      />
      <template #footer>
        <el-button @click="resourceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveResource" :loading="resourceLoading">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog title="绑定用户" v-model="userDialogVisible" width="600px">
      <el-transfer
        v-model="checkedUserIds"
        :data="userTransferData"
        :props="{ key: 'id', label: 'realName' }"
        filterable
        filter-placeholder="搜索用户名/姓名"
      />
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser" :loading="userLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const resourceLoading = ref(false)
const userLoading = ref(false)
const dialogVisible = ref(false)
const resourceDialogVisible = ref(false)
const userDialogVisible = ref(false)
const isAdd = ref(true)
const tableData = ref([])
const orgTree = ref([])
const resourceTree = ref([])
const allUsers = ref([])
const currentRole = ref(null)
const checkedResourceIds = ref([])
const expandedResourceIds = ref([])
const checkedUserIds = ref([])

const searchForm = reactive({
  keyword: '',
  organizationId: ''
})

const roleForm = reactive({
  id: '',
  name: '',
  code: '',
  organizationId: '',
  status: 1,
  description: ''
})

const roleRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  organizationId: [{ required: true, message: '请选择组织机构', trigger: 'change' }]
}

const dialogTitle = computed(() => isAdd.value ? '新增角色' : '编辑角色')

const userTransferData = computed(() => {
  return allUsers.value.map(u => ({
    ...u,
    label: `${u.realName} (${u.username})`
  }))
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.organizationId) params.organizationId = searchForm.organizationId
    
    const res = await request.get('/role/list', { params })
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

const loadResourceTree = async () => {
  try {
    const res = await request.get('/resource/tree')
    resourceTree.value = res.data || []
    expandedResourceIds.value = resourceTree.value.map(r => r.id)
  } catch (e) {
    console.error('加载资源树失败', e)
  }
}

const loadUsers = async () => {
  try {
    const res = await request.get('/user/list')
    allUsers.value = res.data || []
  } catch (e) {
    console.error('加载用户列表失败', e)
  }
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
  roleForm.id = ''
  roleForm.name = ''
  roleForm.code = ''
  roleForm.organizationId = ''
  roleForm.status = 1
  roleForm.description = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isAdd.value = false
  roleForm.id = row.id
  roleForm.name = row.name
  roleForm.code = row.code
  roleForm.organizationId = row.organizationId
  roleForm.status = row.status
  roleForm.description = row.description
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    if (isAdd.value) {
      await request.post('/role', roleForm)
      ElMessage.success('新增成功')
    } else {
      await request.put('/role', roleForm)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleBindResource = async (row) => {
  currentRole.value = row
  checkedResourceIds.value = [...(row.resourceIds || [])]
  resourceDialogVisible.value = true
}

const handleSaveResource = async () => {
  resourceLoading.value = true
  try {
    const checkedIds = resourceTreeRef.value.getCheckedKeys()
    const halfCheckedIds = resourceTreeRef.value.getHalfCheckedKeys()
    const allCheckedIds = [...checkedIds, ...halfCheckedIds]
    
    await request.post('/role/bind-resources', {
      roleId: currentRole.value.id,
      resourceIds: allCheckedIds
    })
    ElMessage.success('绑定成功')
    resourceDialogVisible.value = false
    loadData()
  } finally {
    resourceLoading.value = false
  }
}

const handleBindUser = async (row) => {
  currentRole.value = row
  checkedUserIds.value = [...(row.userIds || [])]
  userDialogVisible.value = true
}

const handleSaveUser = async () => {
  userLoading.value = true
  try {
    await request.post('/role/bind-users', {
      roleId: currentRole.value.id,
      userIds: checkedUserIds.value
    })
    ElMessage.success('绑定成功')
    userDialogVisible.value = false
    loadData()
  } finally {
    userLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/role/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadData()
  loadOrgTree()
  loadResourceTree()
  loadUsers()
})
</script>

<style scoped>
.role-management {
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
