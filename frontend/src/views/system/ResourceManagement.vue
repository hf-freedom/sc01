<template>
  <div class="resource-management">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>资源树</span>
              <el-button type="primary" size="small" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
            </div>
          </template>
          <el-tree
            ref="treeRef"
            :data="resourceTree"
            :props="{ label: 'name', children: 'children' }"
            :highlight-current="true"
            node-key="id"
            default-expand-all
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span>
                  <el-icon v-if="data.type === 0" style="color: #409EFF"><Menu /></el-icon>
                  <el-icon v-else-if="data.type === 1" style="color: #67C23A"><Document /></el-icon>
                  <el-icon v-else style="color: #E6A23C"><Pointer /></el-icon>
                  {{ node.label }}
                </span>
                <span class="node-actions">
                  <el-button type="primary" link size="small" @click.stop="handleEdit(data)">编辑</el-button>
                  <el-button type="danger" link size="small" @click.stop="handleDelete(data)">删除</el-button>
                </span>
              </div>
            </template>
          </el-tree>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card v-if="currentResource">
          <template #header>
            <div class="card-header">
              <span>资源详情</span>
              <el-button :type="currentResource.status === 1 ? 'warning' : 'success'" size="small" @click="handleToggle">
                {{ currentResource.status === 1 ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="资源名称">{{ currentResource.name }}</el-descriptions-item>
            <el-descriptions-item label="资源编码">{{ currentResource.code }}</el-descriptions-item>
            <el-descriptions-item label="资源类型">
              <el-tag :type="getResourceTypeTag(currentResource.type)">
                {{ getResourceTypeName(currentResource.type) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="排序">{{ currentResource.sort }}</el-descriptions-item>
            <el-descriptions-item label="路径">{{ currentResource.path || '-' }}</el-descriptions-item>
            <el-descriptions-item label="组件">{{ currentResource.component || '-' }}</el-descriptions-item>
            <el-descriptions-item label="图标">{{ currentResource.icon || '-' }}</el-descriptions-item>
            <el-descriptions-item label="权限标识">{{ currentResource.permission || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentResource.status === 1 ? 'success' : 'danger'">
                {{ currentResource.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(currentResource.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ currentResource.description || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-divider />
          
          <div class="api-section">
            <div class="api-header">
              <span>关联API接口</span>
              <el-button type="primary" size="small" @click="handleAddApi">
                <el-icon><Plus /></el-icon>
                新增API
              </el-button>
            </div>
            <el-table :data="currentResource.apis || []" v-if="currentResource.apis?.length > 0" size="small">
              <el-table-column prop="name" label="接口名称" width="150" />
              <el-table-column prop="method" label="请求方法" width="100">
                <template #default="scope">
                  <el-tag :type="getMethodTag(scope.row.method)">{{ scope.row.method }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="url" label="接口地址" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ scope.row.status === 1 ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="scope">
                  <el-button type="danger" link size="small" @click="handleDeleteApi(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无关联的API接口" :image-size="60" />
          </div>
        </el-card>
        <el-card v-else>
          <el-empty description="请从左侧选择资源" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="resourceForm" :model="resourceForm" :rules="resourceRules" label-width="100px">
        <el-form-item label="上级资源">
          <el-tree-select
            v-model="resourceForm.parentId"
            :data="resourceTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择上级资源（不选则为顶级资源"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="资源名称" prop="name">
          <el-input v-model="resourceForm.name" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="资源编码" prop="code">
          <el-input v-model="resourceForm.code" placeholder="请输入资源编码" />
        </el-form-item>
        <el-form-item label="资源类型" prop="type">
          <el-radio-group v-model="resourceForm.type">
            <el-radio :label="0">菜单</el-radio>
            <el-radio :label="1">页面</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="resourceForm.path" placeholder="路由路径" />
        </el-form-item>
        <el-form-item label="组件" prop="component">
          <el-input v-model="resourceForm.component" placeholder="组件路径" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="resourceForm.icon" placeholder="图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="resourceForm.sort" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permission">
          <el-input v-model="resourceForm.permission" placeholder="如: user:add, user:edit" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="resourceForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="resourceForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="新增API接口" v-model="apiDialogVisible" width="450px">
      <el-form :model="apiForm" :rules="apiRules" label-width="80px">
        <el-form-item label="接口名称" prop="name">
          <el-input v-model="apiForm.name" placeholder="请输入接口名称" />
        </el-form-item>
        <el-form-item label="请求方法" prop="method">
          <el-select v-model="apiForm.method" placeholder="请选择请求方法" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="接口地址" prop="url">
          <el-input v-model="apiForm.url" placeholder="如: /api/user/list" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="apiForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="apiForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="apiDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveApi" :loading="apiLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const resourceTree = ref([])
const currentResource = ref(null)
const dialogVisible = ref(false)
const apiDialogVisible = ref(false)
const isAdd = ref(true)
const submitLoading = ref(false)
const apiLoading = ref(false)

const resourceForm = reactive({
  id: '',
  name: '',
  code: '',
  parentId: '0',
  type: 0,
  path: '',
  component: '',
  icon: '',
  sort: 1,
  permission: '',
  status: 1,
  description: ''
})

const apiForm = reactive({
  name: '',
  method: 'GET',
  url: '',
  status: 1,
  description: ''
})

const resourceRules = {
  name: [{ required: true, message: '请输入资源名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入资源编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
}

const apiRules = {
  name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
  url: [{ required: true, message: '请输入接口地址', trigger: 'blur' }]
}

const dialogTitle = computed(() => isAdd.value ? '新增资源' : '编辑资源')

const loadData = async () => {
  try {
    const res = await request.get('/resource/tree')
    resourceTree.value = res.data || []
  } catch (e) {
    console.error('加载资源树失败', e)
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const getResourceTypeName = (type) => {
  const types = { 0: '菜单', 1: '页面', 2: '按钮' }
  return types[type] || '未知'
}

const getResourceTypeTag = (type) => {
  const tags = { 0: 'primary', 1: 'success', 2: 'warning' }
  return tags[type] || 'info'
}

const getMethodTag = (method) => {
  const tags = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return tags[method] || 'info'
}

const handleNodeClick = (data) => {
  currentResource.value = { ...data }
}

const handleAdd = () => {
  isAdd.value = true
  resourceForm.id = ''
  resourceForm.name = ''
  resourceForm.code = ''
  resourceForm.parentId = '0'
  resourceForm.type = 0
  resourceForm.path = ''
  resourceForm.component = ''
  resourceForm.icon = ''
  resourceForm.sort = 1
  resourceForm.permission = ''
  resourceForm.status = 1
  resourceForm.description = ''
  dialogVisible.value = true
}

const handleEdit = (data) => {
  isAdd.value = false
  resourceForm.id = data.id
  resourceForm.name = data.name
  resourceForm.code = data.code
  resourceForm.parentId = data.parentId
  resourceForm.type = data.type
  resourceForm.path = data.path
  resourceForm.component = data.component
  resourceForm.icon = data.icon
  resourceForm.sort = data.sort
  resourceForm.permission = data.permission
  resourceForm.status = data.status
  resourceForm.description = data.description
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    if (isAdd.value) {
      await request.post('/resource', resourceForm)
      ElMessage.success('新增成功')
    } else {
      await request.put('/resource', resourceForm)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    loadData()
    currentResource.value = null
  } finally {
    submitLoading.value = false
  }
}

const handleToggle = async () => {
  const action = currentResource.value.status === 1 ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该资源吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.put(`/resource/toggle/${currentResource.value.id}`)
    ElMessage.success(`${action}成功`)
    currentResource.value.status = currentResource.value.status === 1 ? 0 : 1
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleDelete = async (data) => {
  try {
    await ElMessageBox.confirm('确定要删除该资源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/resource/${data.id}`)
    ElMessage.success('删除成功')
    loadData()
    currentResource.value = null
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

const handleAddApi = () => {
  apiForm.name = ''
  apiForm.method = 'GET'
  apiForm.url = ''
  apiForm.status = 1
  apiForm.description = ''
  apiDialogVisible.value = true
}

const handleSaveApi = async () => {
  apiLoading.value = true
  try {
    await request.post(`/resource/${currentResource.value.id}/api`, apiForm)
    ElMessage.success('新增成功')
    apiDialogVisible.value = false
    const res = await request.get(`/resource/${currentResource.value.id}`)
    currentResource.value = res.data
    loadData()
  } finally {
    apiLoading.value = false
  }
}

const handleDeleteApi = async (api) => {
  try {
    await ElMessageBox.confirm('确定要删除该API接口吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/resource/${currentResource.value.id}/api/${api.id}`)
    ElMessage.success('删除成功')
    currentResource.value.apis = currentResource.value.apis.filter(a => a.id !== api.id)
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.resource-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-node {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  padding-right: 8px;
}

.node-actions {
  visibility: hidden;
}

.tree-node:hover .node-actions {
  visibility: visible;
}

.api-section {
  margin-top: 10px;
}

.api-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-weight: bold;
}
</style>
