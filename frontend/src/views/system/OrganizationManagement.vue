<template>
  <div class="organization-management">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>组织机构</span>
              <el-button type="primary" size="small" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
            </div>
          </template>
          <el-tree
            ref="treeRef"
            :data="orgTree"
            :props="{ label: 'name', children: 'children' }"
            :highlight-current="true"
            node-key="id"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span>{{ node.label }}</span>
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
        <el-card>
          <template #header>
            <span>{{ currentOrg ? '机构详情' : '请选择组织机构' }}</span>
          </template>
          <el-descriptions v-if="currentOrg" :column="2" border>
            <el-descriptions-item label="机构名称">{{ currentOrg.name }}</el-descriptions-item>
            <el-descriptions-item label="机构编码">{{ currentOrg.code }}</el-descriptions-item>
            <el-descriptions-item label="层级">{{ currentOrg.level }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ currentOrg.sort }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentOrg.status === 1 ? 'success' : 'danger'">
                {{ currentOrg.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(currentOrg.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ currentOrg.description || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="请从左侧选择组织机构" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="orgForm" :rules="orgRules" label-width="100px">
        <el-form-item label="上级机构">
          <el-tree-select
            v-model="orgForm.parentId"
            :data="orgTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择上级机构（不选则为顶级机构"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="机构名称" prop="name">
          <el-input v-model="orgForm.name" placeholder="请输入机构名称" />
        </el-form-item>
        <el-form-item label="机构编码" prop="code">
          <el-input v-model="orgForm.code" placeholder="请输入机构编码" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="orgForm.sort" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="orgForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="orgForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
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

const orgTree = ref([])
const currentOrg = ref(null)
const dialogVisible = ref(false)
const isAdd = ref(true)
const submitLoading = ref(false)

const orgForm = reactive({
  id: '',
  name: '',
  code: '',
  parentId: '0',
  sort: 1,
  status: 1,
  description: ''
})

const orgRules = {
  name: [{ required: true, message: '请输入机构名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => isAdd.value ? '新增组织机构' : '编辑组织机构')

const loadData = async () => {
  try {
    const res = await request.get('/organization/tree')
    orgTree.value = res.data || []
  } catch (e) {
    console.error('加载组织机构失败', e)
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const handleNodeClick = (data) => {
  currentOrg.value = data
}

const handleAdd = () => {
  isAdd.value = true
  orgForm.id = ''
  orgForm.name = ''
  orgForm.code = ''
  orgForm.parentId = '0'
  orgForm.sort = 1
  orgForm.status = 1
  orgForm.description = ''
  dialogVisible.value = true
}

const handleEdit = (data) => {
  isAdd.value = false
  orgForm.id = data.id
  orgForm.name = data.name
  orgForm.code = data.code
  orgForm.parentId = data.parentId
  orgForm.sort = data.sort
  orgForm.status = data.status
  orgForm.description = data.description
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    if (isAdd.value) {
      await request.post('/organization', orgForm)
      ElMessage.success('新增成功')
    } else {
      await request.put('/organization', orgForm)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    loadData()
    currentOrg.value = null
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (data) => {
  try {
    await ElMessageBox.confirm('确定要删除该组织机构吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/organization/${data.id}`)
    ElMessage.success('删除成功')
    loadData()
    currentOrg.value = null
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.organization-management {
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
</style>
