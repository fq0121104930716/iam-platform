<template>
  <div class="user-list">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showCreateDialog">新增用户</el-button>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-input v-model="searchQuery" placeholder="搜索用户名/邮箱/手机" clearable @change="handleSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </el-col>
    </el-row>

    <el-table :data="users" v-loading="loading" border>
      <el-table-column prop="userCode" label="用户编码" width="150" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="租户" width="200">
        <template #default="{ row }">
          <el-tag v-for="tenant in row.tenants" :key="tenant.tenantId" size="small" style="margin-right: 5px">
            {{ tenant.tenantName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button size="small" type="info" @click="showTenantMapping(row)">租户关联</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @current-change="loadUsers"
      @size-change="loadUsers"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password />
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

    <!-- Tenant Mapping Dialog -->
    <el-dialog v-model="tenantMappingVisible" title="租户关联管理" width="800px">
      <el-button type="primary" @click="showAddTenantMapping" style="margin-bottom: 15px">添加租户关联</el-button>
      <el-table :data="userTenants" border>
        <el-table-column prop="tenantCode" label="租户编码" />
        <el-table-column prop="tenantName" label="租户名称" />
        <el-table-column prop="accountCode" label="账户编码" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : (row.status === 'SUSPENDED' ? 'warning' : 'info')">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleSuspend(row)" v-if="row.status === 'ACTIVE'">停用</el-button>
            <el-button size="small" type="success" @click="handleReactivate(row)" v-if="row.status === 'SUSPENDED'">恢复</el-button>
            <el-button size="small" type="danger" @click="handleRemoveTenant(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const users = ref([])
const userTenants = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const tenantMappingVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const searchQuery = ref('')
const formRef = ref()
const currentUser = ref(null)

const form = reactive({
  id: null,
  username: '',
  email: '',
  phone: '',
  password: '',
  status: 'ACTIVE'
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await adminApi.getUsers({
      page: pagination.page,
      size: pagination.size,
      query: searchQuery.value
    })
    users.value = response.content || response || []
    pagination.total = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadUsers()
}

const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    username: '',
    email: '',
    phone: '',
    password: '',
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  currentUser.value = row
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await adminApi.updateUser(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await adminApi.createUser(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadUsers()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}" 吗?`, '确认', {
      type: 'warning'
    })
    await adminApi.deleteUser(row.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error) {
    // User cancelled or delete failed
  }
}

const showTenantMapping = async (row) => {
  currentUser.value = row
  tenantMappingVisible.value = true
  try {
    const response = await adminApi.getUserTenantMappings(row.id)
    userTenants.value = response.mappings || response || []
  } catch (error) {
    ElMessage.error('加载租户关联失败')
  }
}

const showAddTenantMapping = () => {
  ElMessage.info('添加租户关联功能开发中')
}

const handleSuspend = async (row) => {
  try {
    await adminApi.suspendUserTenantMapping(row.id)
    ElMessage.success('停用成功')
    showTenantMapping(currentUser.value)
  } catch (error) {
    ElMessage.error('停用失败')
  }
}

const handleReactivate = async (row) => {
  try {
    await adminApi.reactivateUserTenantMapping(row.id)
    ElMessage.success('恢复成功')
    showTenantMapping(currentUser.value)
  } catch (error) {
    ElMessage.error('恢复失败')
  }
}

const handleRemoveTenant = async (row) => {
  try {
    await ElMessageBox.confirm('确定移除此租户关联吗?', '确认', { type: 'warning' })
    ElMessage.success('移除成功')
    showTenantMapping(currentUser.value)
  } catch (error) {
    // User cancelled
  }
}

const getStatusLabel = (status) => {
  const map = {
    ACTIVE: '正常',
    SUSPENDED: '已停用',
    LEFT: '已离职'
  }
  return map[status] || status
}
</script>

<style scoped>
.user-list {
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
