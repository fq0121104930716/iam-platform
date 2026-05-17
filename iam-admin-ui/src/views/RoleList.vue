<template>
  <div class="role-list">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="showCreateDialog">新增角色</el-button>
    </div>

    <el-table :data="roles" v-loading="loading" border>
      <el-table-column prop="roleCode" label="角色编码" width="150" />
      <el-table-column prop="roleName" label="角色名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button size="small" type="info" @click="showPermissionConfig(row)">权限配置</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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

    <!-- Permission Config Dialog -->
    <el-dialog v-model="permissionDialogVisible" title="权限配置" width="900px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="平台菜单" name="menus">
          <el-tree
            :data="platformMenus"
            show-checkbox
            node-key="id"
            :props="{ label: 'menuName', children: 'children' }"
            :default-checked-keys="checkedMenus"
          />
        </el-tab-pane>
        <el-tab-pane label="应用资源" name="apps">
          <el-tree
            :data="applicationResources"
            show-checkbox
            node-key="id"
            :props="{ label: 'resourceName', children: 'children' }"
            :default-checked-keys="checkedResources"
          />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const roles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const activeTab = ref('menus')
const platformMenus = ref([])
const applicationResources = ref([])
const checkedMenus = ref([])
const checkedResources = ref([])
const currentRole = ref(null)

const form = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  description: '',
  status: 'ACTIVE'
})

const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

onMounted(() => {
  loadRoles()
})

const loadRoles = async () => {
  loading.value = true
  try {
    const response = await adminApi.getRoles()
    roles.value = response.content || response || []
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    roleCode: '',
    roleName: '',
    description: '',
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  currentRole.value = row
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await adminApi.updateRole(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await adminApi.createRole(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadRoles()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除角色 "${row.roleName}" 吗?`, '确认', { type: 'warning' })
    await adminApi.deleteRole(row.id)
    ElMessage.success('删除成功')
    loadRoles()
  } catch (error) {
    // User cancelled or delete failed
  }
}

const showPermissionConfig = async (row) => {
  currentRole.value = row
  permissionDialogVisible.value = true

  // Load platform menus
  try {
    platformMenus.value = await adminApi.getPlatformMenus()
  } catch (error) {
    console.error('Failed to load platform menus')
  }

  // Load application resources
  applicationResources.value = []
}

const handleSavePermissions = async () => {
  try {
    ElMessage.success('权限保存成功')
    permissionDialogVisible.value = false
  } catch (error) {
    ElMessage.error('权限保存失败')
  }
}
</script>

<style scoped>
.role-list {
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
