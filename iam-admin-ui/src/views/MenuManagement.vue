<template>
  <div class="menu-management">
    <div class="page-header">
      <h2>菜单管理</h2>
      <el-button type="primary" @click="showCreateDialog">新增平台菜单</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="平台菜单" name="platform">
        <el-table :data="platformMenus" v-loading="loading" border row-key="id" :tree-props="{ children: 'children' }">
          <el-table-column prop="menuCode" label="菜单编码" width="150" />
          <el-table-column prop="menuName" label="菜单名称" />
          <el-table-column prop="icon" label="图标" width="100" />
          <el-table-column prop="path" label="路由路径" />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column prop="visible" label="可见" width="80">
            <template #default="{ row }">
              <el-tag :type="row.visible ? 'success' : 'info'">
                {{ row.visible ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="租户配置" name="tenant">
        <el-row :gutter="20" style="margin-bottom: 20px">
          <el-col :span="6">
            <el-select v-model="selectedTenant" placeholder="选择租户" @change="loadTenantMenus">
              <el-option
                v-for="tenant in tenants"
                :key="tenant.tenantId"
                :label="tenant.tenantName"
                :value="tenant.tenantId"
              />
            </el-select>
          </el-col>
        </el-row>

        <el-table :data="tenantMenus" v-loading="tenantLoading" border>
          <el-table-column prop="menuCode" label="菜单编码" />
          <el-table-column prop="menuName" label="菜单名称" />
          <el-table-column prop="enabled" label="是否启用" width="120">
            <template #default="{ row }">
              <el-switch
                v-model="row.enabled"
                @change="handleToggleMenu(row)"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '新增菜单'"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="父级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeOptions"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            check-strictly
            placeholder="选择父级菜单（可选）"
          />
        </el-form-item>
        <el-form-item label="菜单编码" prop="menuCode">
          <el-input v-model="form.menuCode" />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="如: Odometer" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="是否可见">
          <el-switch v-model="form.visible" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { adminApi } from '@/api/admin'

const activeTab = ref('platform')
const platformMenus = ref([])
const tenantMenus = ref([])
const tenants = ref([])
const selectedTenant = ref(null)
const loading = ref(false)
const tenantLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  parentId: null,
  menuCode: '',
  menuName: '',
  icon: '',
  path: '',
  sortOrder: 0,
  description: '',
  visible: true
})

const rules = {
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

const menuTreeOptions = computed(() => {
  return [{ id: null, menuName: '顶级菜单', children: platformMenus.value }]
})

onMounted(() => {
  loadPlatformMenus()
  loadTenants()
})

const loadPlatformMenus = async () => {
  loading.value = true
  try {
    platformMenus.value = await adminApi.getPlatformMenus()
  } catch (error) {
    ElMessage.error('加载平台菜单失败')
  } finally {
    loading.value = false
  }
}

const loadTenants = async () => {
  try {
    const response = await adminApi.getTenants()
    tenants.value = response.content || response || []
  } catch (error) {
    ElMessage.error('加载租户列表失败')
  }
}

const loadTenantMenus = async () => {
  if (!selectedTenant.value) return

  tenantLoading.value = true
  try {
    tenantMenus.value = await adminApi.getTenantMenus(selectedTenant.value)
  } catch (error) {
    ElMessage.error('加载租户菜单失败')
  } finally {
    tenantLoading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    parentId: null,
    menuCode: '',
    menuName: '',
    icon: '',
    path: '',
    sortOrder: 0,
    description: '',
    visible: true
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
        await adminApi.updateMenu(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await adminApi.createMenu(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadPlatformMenus()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除菜单 "${row.menuName}" 吗?`, '确认', { type: 'warning' })
    await adminApi.deleteMenu(row.id)
    ElMessage.success('删除成功')
    loadPlatformMenus()
  } catch (error) {
    // User cancelled or delete failed
  }
}

const handleToggleMenu = async (row) => {
  try {
    await adminApi.configureTenantMenu(selectedTenant.value, row.menuId, row.enabled)
    ElMessage.success('配置成功')
  } catch (error) {
    ElMessage.error('配置失败')
    row.enabled = !row.enabled // Revert
  }
}
</script>

<style scoped>
.menu-management {
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
