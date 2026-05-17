<template>
  <div class="tenant-list">
    <div class="page-header">
      <h2>租户管理</h2>
      <el-button type="primary" @click="showCreateDialog">新增租户</el-button>
    </div>

    <el-table :data="tenants" v-loading="loading" border>
      <el-table-column prop="tenantCode" label="租户编码" />
      <el-table-column prop="tenantName" label="租户名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="maxUsers" label="最大用户数" width="120" />
      <el-table-column prop="expirationDate" label="到期时间" width="150" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button size="small" type="info" @click="showMenuConfig(row)">菜单配置</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑租户' : '新增租户'"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="租户编码" prop="tenantCode">
          <el-input v-model="form.tenantCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="租户名称" prop="tenantName">
          <el-input v-model="form.tenantName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="最大用户数">
          <el-input-number v-model="form.maxUsers" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const tenants = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  tenantCode: '',
  tenantName: '',
  description: '',
  maxUsers: 100,
  status: 'ACTIVE'
})

const rules = {
  tenantCode: [{ required: true, message: '请输入租户编码', trigger: 'blur' }],
  tenantName: [{ required: true, message: '请输入租户名称', trigger: 'blur' }]
}

onMounted(() => {
  loadTenants()
})

const loadTenants = async () => {
  loading.value = true
  try {
    const response = await adminApi.getTenants()
    tenants.value = response.content || response || []
  } catch (error) {
    ElMessage.error('加载租户列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    tenantCode: '',
    tenantName: '',
    description: '',
    maxUsers: 100,
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await adminApi.updateTenant(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await adminApi.createTenant(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadTenants()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除租户 "${row.tenantName}" 吗?`, '确认', {
      type: 'warning'
    })
    await adminApi.deleteTenant(row.id)
    ElMessage.success('删除成功')
    loadTenants()
  } catch (error) {
    // User cancelled or delete failed
  }
}

const showMenuConfig = (row) => {
  ElMessage.info('菜单配置功能开发中')
}
</script>

<style scoped>
.tenant-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}
</style>
